package com.example.toycamping.data.source.local

import com.example.toycamping.room.CampingDatabase
import com.example.toycamping.room.CampingEntity
import com.example.toycamping.utils.Result
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.inject

class GoCampingLocalDataSourceImpl : GoCampingLocalDataSource {

    private val campingDatabase by inject<CampingDatabase>(CampingDatabase::class.java)

    override suspend fun getAllCampingData(): Result<List<CampingEntity>> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                Result.Success(campingDatabase.campingDao().getAll())
            } catch (e: Exception) {
                Result.Error(Exception(Throwable("Error GetAllCampingEntity")))
            }
        }

    override suspend fun checkExistCampingData(): Boolean = withContext(Dispatchers.IO) {
        return@withContext campingDatabase.campingDao().getAll().isNotEmpty()
    }

    override suspend fun registerCampingData(campingEntity: CampingEntity): Boolean =
        withContext(Dispatchers.IO) {
            return@withContext campingDatabase.campingDao()
                .registerCampingEntity(campingEntity) > 0
        }



    override suspend fun getCampingData(
        name: String
    ): Result<CampingEntity> = withContext(Dispatchers.IO) {
        return@withContext try {
            if (isExistCampingEntity(name)) {
                Result.Success(campingDatabase.campingDao().getCampingEntity(name))
            } else {
                Result.Error(Exception(Throwable("Not Exist Data")))
            }
        } catch (e: Exception) {
            Result.Error(Exception(Throwable("GetCampingData Error!")))
        }
    }

    override suspend fun isExistCampingEntity(name: String): Boolean = withContext(Dispatchers.IO) {
        return@withContext campingDatabase.campingDao().isExistCampingEntity(name) > 0L
    }
}