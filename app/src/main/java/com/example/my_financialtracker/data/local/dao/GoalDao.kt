package com.example.my_financialtracker.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.example.my_financialtracker.data.local.entity.GoalEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface GoalDao {
    @Query("SELECT * FROM goals ORDER BY deadlineAt ASC LIMIT 1")
    fun observePrimaryGoal(): Flow<GoalEntity?>

    @Query("SELECT * FROM goals ORDER BY deadlineAt ASC")
    fun observeAllGoals(): Flow<List<GoalEntity>>

    @Query("SELECT * FROM goals ORDER BY deadlineAt ASC LIMIT 1")
    suspend fun getPrimaryGoal(): GoalEntity?

    @Query("SELECT * FROM goals ORDER BY deadlineAt ASC")
    suspend fun getAllGoals(): List<GoalEntity>

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsert(goal: GoalEntity)

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun upsertAll(goals: List<GoalEntity>)

    @Query("SELECT * FROM goals WHERE id = :goalId LIMIT 1")
    suspend fun getGoalById(goalId: String): GoalEntity?
}
