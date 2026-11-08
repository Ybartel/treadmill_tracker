package com.naynff.treadmilltracker.data

import android.content.Context
import android.database.sqlite.SQLiteDatabase
import androidx.room.Room
import androidx.test.core.app.ApplicationProvider
import androidx.test.ext.junit.runners.AndroidJUnit4
import com.naynff.treadmilltracker.data.local.TreadmillDatabase
import com.naynff.treadmilltracker.data.local.WorkoutSessionDao
import com.naynff.treadmilltracker.data.local.WorkoutSessionEntity
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.runTest
import org.junit.After
import org.junit.Assert.assertEquals
import org.junit.Assert.assertNull
import org.junit.Before
import org.junit.Test
import org.junit.runner.RunWith
import java.time.Instant

private const val MIGRATION_DB = "migration-test.db"

/** Version 1 as exported in schemas/.../1.json. */
private val VERSION_1_SCHEMA = listOf(
    "CREATE TABLE `treadmill` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, `position` INTEGER NOT NULL, " +
        "`isActive` INTEGER NOT NULL)",
    "CREATE UNIQUE INDEX `index_treadmill_position` ON `treadmill` (`position`)",
    "CREATE TABLE `treadmill_review` (`id` INTEGER PRIMARY KEY AUTOINCREMENT NOT NULL, " +
        "`treadmillId` INTEGER NOT NULL, `condition` TEXT NOT NULL, `comment` TEXT, `reviewedAt` INTEGER NOT NULL, " +
        "FOREIGN KEY(`treadmillId`) REFERENCES `treadmill`(`id`) ON UPDATE NO ACTION ON DELETE NO ACTION )",
    "CREATE INDEX `index_treadmill_review_treadmillId` ON `treadmill_review` (`treadmillId`)",
    "CREATE TABLE room_master_table (id INTEGER PRIMARY KEY,identity_hash TEXT)",
    "INSERT INTO room_master_table (id,identity_hash) VALUES(42, '4bdc795a89afda6a689af40a1c624003')",
)

@RunWith(AndroidJUnit4::class)
class WorkoutSessionDaoTest {
    private lateinit var database: TreadmillDatabase
    private lateinit var dao: WorkoutSessionDao

    @Before
    fun setUp() {
        database = Room.inMemoryDatabaseBuilder(
            ApplicationProvider.getApplicationContext(),
            TreadmillDatabase::class.java,
        ).build()
        dao = database.workoutSessionDao()
    }

    @After
    fun tearDown() = database.close()

    private fun session(epochSecond: Long, steps: Int? = null, id: Long = 0) =
        WorkoutSessionEntity(id, 45, 65, steps, Instant.ofEpochSecond(epochSecond))

    @Test
    fun sessionsComeMostRecentFirst() = runTest {
        dao.upsert(session(100))
        dao.upsert(session(300))
        dao.upsert(session(200))

        assertEquals(listOf(300L, 200L, 100L), dao.observeAll().first().map { it.endedAt.epochSecond })
        assertEquals(300L, dao.getLatest()?.endedAt?.epochSecond)
    }

    @Test
    fun upsertUpdatesAnExistingSessionAndRestoresADeletedOne() = runTest {
        val id = dao.upsert(session(100))
        dao.upsert(session(100, steps = 5310, id = id))
        assertEquals(5310, dao.getById(id)?.steps)

        val saved = checkNotNull(dao.getById(id))
        dao.delete(id)
        assertNull(dao.getById(id))
        dao.upsert(saved)
        assertEquals(saved, dao.getById(id))
    }

    /** Room checks the migrated schema against version 2 when it opens the database. */
    @Test
    fun migrationFromVersion1KeepsReviewsAndAddsTheSessionTable() = runTest {
        val context = ApplicationProvider.getApplicationContext<Context>()
        context.deleteDatabase(MIGRATION_DB)
        SQLiteDatabase.openOrCreateDatabase(context.getDatabasePath(MIGRATION_DB), null).use { db ->
            VERSION_1_SCHEMA.forEach(db::execSQL)
            db.execSQL("INSERT INTO treadmill (id, position, isActive) VALUES (1, 1, 1)")
            db.execSQL(
                "INSERT INTO treadmill_review (treadmillId, condition, comment, reviewedAt) " +
                    "VALUES (1, 'WORKING', NULL, 1000)",
            )
            db.version = 1
        }

        val migrated = Room.databaseBuilder(context, TreadmillDatabase::class.java, MIGRATION_DB).build()
        try {
            val treadmill = migrated.treadmillDao().observeActiveWithLatestReview().first().single()
            assertEquals(Instant.ofEpochMilli(1000), treadmill.reviewedAt)
            migrated.workoutSessionDao().upsert(session(2000))
            assertEquals(1, migrated.workoutSessionDao().observeAll().first().size)
        } finally {
            migrated.close()
            context.deleteDatabase(MIGRATION_DB)
        }
    }
}
