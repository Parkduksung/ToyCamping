package com.example.toycamping.data.source.loca

import com.example.toycamping.room.database.CampingDatabase
import com.example.toycamping.room.entity.CampingEntity
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.withContext
import org.koin.java.KoinJavaComponent.inject

class GoCampingLocalDataSourceImpl : GoCampingLocalDataSource {

    private val campingDatabase by inject<CampingDatabase>(CampingDatabase::class.java)

    override suspend fun getAllCampingData(): Result<List<CampingEntity>> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                Result.success(campingDatabase.campingDao().getAll())
            } catch (e: Exception) {
                Result.failure(Throwable("Error GetAllCampingEntity"))
            }
        }

    override suspend fun toggleBookmarkCampingData(item: CampingEntity): Result<CampingEntity> =
        withContext(Dispatchers.IO) {
            val updateCampingData = campingDatabase.campingDao().updateBookmarkCampingData(
                name = item.name,
                address = item.address,
                like = !item.like
            )
            return@withContext if (updateCampingData == 1) {
                val updateVocaEntity = item.copy(like = !item.like)
                Result.success(updateVocaEntity)
            } else {
                Result.failure(Throwable("Error ToggleBookmark"))
            }
        }

    override suspend fun getAllBookmarkList(): Result<List<CampingEntity>> =
        withContext(Dispatchers.IO) {
            return@withContext try {
                val getAllBookmarkList =
                    campingDatabase.campingDao().getBookmarkCampingEntity(true)
                Result.success(getAllBookmarkList)
            } catch (e: Exception) {
                Result.failure(Throwable("bookmarkList is Null!"))
            }
        }

    override suspend fun checkExistCampingData(): Boolean = withContext(Dispatchers.IO) {
        return@withContext campingDatabase.campingDao().getAll().isNotEmpty()
    }

    override suspend fun checkExistCampingData(item: CampingEntity): Boolean =
        withContext(Dispatchers.IO) {
            return@withContext try {
                campingDatabase.campingDao()
                    .checkCampingEntity(name = item.name, address = item.address)
                true
            } catch (e: Exception) {
                false
            }
        }

    override suspend fun registerCampingData(campingEntity: CampingEntity): Boolean =
        withContext(Dispatchers.IO) {
            return@withContext campingDatabase.campingDao()
                .registerCampingEntity(campingEntity) > 0
        }
}