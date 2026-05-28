package com.splunk.android.common.storage.filemanager

import android.os.Build
import android.util.Log
import com.splunk.android.common.storage.encryption.KeyManager
import java.security.UnrecoverableKeyException

object FileManagerFactory {

    fun createEncryptedFileManagerIfPossible(keyAlias: String): IFileManager {
        if (Build.VERSION.SDK_INT < Build.VERSION_CODES.M) {
            return PlainFileManager()
        }

        val secretKey = try {
            KeyManager.instance.getKey(keyAlias)
        } catch (e: UnrecoverableKeyException) {
            Log.e(TAG, "createEncryptedFileManagerIfPossible(keyAlias: $keyAlias)", e)

            null
        } catch (e: Exception) {
            Log.e(TAG, "createEncryptedFileManagerIfPossible(keyAlias: $keyAlias)", e)

            null
        }

        return if (secretKey == null) PlainFileManager() else EncryptedFileManager(secretKey)
    }

    fun createConditionedEncryptedFileManager(keyAlias: String, isEncrypted: Boolean): IFileManager {
        return if (isEncrypted) createEncryptedFileManagerIfPossible(keyAlias) else PlainFileManager()
    }

    fun createPlainFileManager(): IFileManager {
        return PlainFileManager()
    }

    private const val TAG = "FileManagerFactory"
}
