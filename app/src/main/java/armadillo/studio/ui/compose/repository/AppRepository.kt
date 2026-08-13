package armadillo.studio.ui.compose.repository

import armadillo.studio.common.base.callback.SocketCallBack
import armadillo.studio.common.base.callback.TaskCallBack
import armadillo.studio.common.enums.SoftEnums
import armadillo.studio.helper.InjectInfo
import armadillo.studio.helper.SocketHelper
import armadillo.studio.model.Basic
import armadillo.studio.model.soft.UserSoft
import armadillo.studio.model.sys.Notice
import armadillo.studio.model.sys.Other
import armadillo.studio.model.sys.TaskInfo
import armadillo.studio.model.sys.Ver
import kotlinx.coroutines.CancellableContinuation
import kotlinx.coroutines.suspendCancellableCoroutine
import kotlinx.coroutines.withTimeoutOrNull
import kotlin.coroutines.resume
import kotlin.coroutines.resumeWithException
import java.io.IOException

/**
 * Singleton repository that bridges the existing Java [SocketHelper] callback-based
 * API with Kotlin coroutines.
 *
 * Each method uses [suspendCancellableCoroutine] to convert a callback invocation
 * into a suspending call.  Anonymous [SocketCallBack] objects are created with
 * **concrete** type arguments (e.g. `SocketCallBack<Basic>`) because the Java
 * [SocketHelper] uses reflection on the callback's generic interface to determine
 * the Gson deserialisation target class.
 *
 * For [TaskCallBack] (which only exposes [TaskCallBack.Next] and has no error
 * callback) a timeout is applied via [withTimeoutOrNull] so that the coroutine
 * does not hang indefinitely when the request fails silently.
 */
object AppRepository {

    /** Timeout for [getTaskInfo] in milliseconds (matches SocketHelper's socket timeout). */
    private const val TASK_TIMEOUT_MS = 15_000L

    // -----------------------------------------------------------------
    //  Continuation helpers
    // -----------------------------------------------------------------

    /**
     * Resumes [this] continuation with [value] when non-null, otherwise resumes
     * with a [NullPointerException].  No-ops if the continuation is no longer
     * active (e.g. cancelled).
     */
    private fun <T> CancellableContinuation<T>.safeResume(value: T?) {
        if (!isActive) return
        if (value != null) {
            resume(value)
        } else {
            resumeWithException(NullPointerException("Response body is null"))
        }
    }

    /**
     * Resumes [this] continuation with an exception.  If [throwable] is null a
     * generic [RuntimeException] is used instead.  No-ops if the continuation is
     * no longer active.
     */
    private fun <T> CancellableContinuation<T>.safeResumeWithException(throwable: Throwable?) {
        if (!isActive) return
        resumeWithException(throwable ?: RuntimeException("Unknown error"))
    }

    // -----------------------------------------------------------------
    //  Auth
    // -----------------------------------------------------------------

    /** Account + password login. */
    suspend fun login(username: String, password: String): Basic =
        suspendCancellableCoroutine { cont ->
            SocketHelper.UserHelper.UserNameLogin(object : SocketCallBack<Basic> {
                override fun next(body: Basic?) {
                    cont.safeResume(body)
                }

                override fun error(throwable: Throwable?) {
                    cont.safeResumeWithException(throwable)
                }
            }, username, password)
        }

    /** Account + password + email registration. */
    suspend fun register(username: String, password: String, email: String): Basic =
        suspendCancellableCoroutine { cont ->
            SocketHelper.UserHelper.UserRegistered(object : SocketCallBack<Basic> {
                override fun next(body: Basic?) {
                    cont.safeResume(body)
                }

                override fun error(throwable: Throwable?) {
                    cont.safeResumeWithException(throwable)
                }
            }, username, password, email)
        }

    /** Third-party (QQ) login via openid. */
    suspend fun loginWithQQ(openid: String): Basic =
        suspendCancellableCoroutine { cont ->
            SocketHelper.UserHelper.UserLogin(object : SocketCallBack<Basic> {
                override fun next(body: Basic?) {
                    cont.safeResume(body)
                }

                override fun error(throwable: Throwable?) {
                    cont.safeResumeWithException(throwable)
                }
            }, openid)
        }

    // -----------------------------------------------------------------
    //  Software list
    // -----------------------------------------------------------------

    /** Paginated list of the current user's applications. */
    suspend fun getSoftList(offset: Int, limit: Int): UserSoft =
        suspendCancellableCoroutine { cont ->
            SocketHelper.UserHelper.GetSoft(object : SocketCallBack<UserSoft> {
                override fun next(body: UserSoft?) {
                    cont.safeResume(body)
                }

                override fun error(throwable: Throwable?) {
                    cont.safeResumeWithException(throwable)
                }
            }, offset, limit)
        }

    // -----------------------------------------------------------------
    //  Tasks
    // -----------------------------------------------------------------

    /**
     * Retrieves task status information for [uuid].
     *
     * The underlying Java API uses [TaskCallBack] which has **no** error callback,
     * so a [withTimeoutOrNull] guard is applied to prevent the coroutine from
     * hanging forever when the request fails silently.
     */
    suspend fun getTaskInfo(uuid: String): TaskInfo =
        withTimeoutOrNull(TASK_TIMEOUT_MS) {
            suspendCancellableCoroutine { cont ->
                SocketHelper.SysHelper.GetTaskInfo(object : TaskCallBack<TaskInfo> {
                    override fun Next(body: TaskInfo?) {
                        cont.safeResume(body)
                    }
                }, uuid)
            }
        } ?: throw IOException("获取任务信息超时")

    /**
     * Submits a new protection task.
     *
     * @param injectInfo pre-built [InjectInfo] containing token, handle enums,
     *                   uuid, rule and md5.
     * @param packName   the package name of the APK being processed.
     */
    suspend fun submitTask(injectInfo: InjectInfo, packName: String): Basic =
        suspendCancellableCoroutine { cont ->
            SocketHelper.SysHelper.SubmitTask(object : SocketCallBack<Basic> {
                override fun next(body: Basic?) {
                    cont.safeResume(body)
                }

                override fun error(throwable: Throwable?) {
                    cont.safeResumeWithException(throwable)
                }
            }, injectInfo, packName)
        }

    // -----------------------------------------------------------------
    //  System
    // -----------------------------------------------------------------

    /** Retrieves the latest system notice. */
    suspend fun getNotice(): Notice =
        suspendCancellableCoroutine { cont ->
            SocketHelper.SysHelper.GetSysNotice(object : SocketCallBack<Notice> {
                override fun next(body: Notice?) {
                    cont.safeResume(body)
                }

                override fun error(throwable: Throwable?) {
                    cont.safeResumeWithException(throwable)
                }
            })
        }

    /** Retrieves the latest app version info. */
    suspend fun getVersion(): Ver =
        suspendCancellableCoroutine { cont ->
            SocketHelper.SysHelper.GetVersion(object : SocketCallBack<Ver> {
                override fun next(body: Ver?) {
                    cont.safeResume(body)
                }

                override fun error(throwable: Throwable?) {
                    cont.safeResumeWithException(throwable)
                }
            })
        }

    // -----------------------------------------------------------------
    //  Software module operations
    // -----------------------------------------------------------------

    /**
     * Retrieves module-specific configuration for an application.
     *
     * The return type is [Basic] because the concrete model class varies by
     * [SoftEnums] (e.g. SoftNoticeInfo, SoftUpdateInfo, SoftAdmobInfo, …) and
     * cannot be determined statically.
     */
    suspend fun getSoftModelInfo(appkey: String, softEnums: SoftEnums): Basic =
        suspendCancellableCoroutine { cont ->
            SocketHelper.UserHelper.GetSoftModelInfo(object : SocketCallBack<Basic> {
                override fun next(body: Basic?) {
                    cont.safeResume(body)
                }

                override fun error(throwable: Throwable?) {
                    cont.safeResumeWithException(throwable)
                }
            }, appkey, softEnums)
        }

    /** Saves module-specific configuration for an application. */
    suspend fun saveSoftModelInfo(appkey: String, softEnums: SoftEnums, info: String): Basic =
        suspendCancellableCoroutine { cont ->
            SocketHelper.UserHelper.SaveSoftModelInfo(object : SocketCallBack<Basic> {
                override fun next(body: Basic?) {
                    cont.safeResume(body)
                }

                override fun error(throwable: Throwable?) {
                    cont.safeResumeWithException(throwable)
                }
            }, appkey, softEnums, info)
        }

    /** Deletes an application by its appkey. */
    suspend fun deleteSoft(appkey: String): Basic =
        suspendCancellableCoroutine { cont ->
            SocketHelper.UserHelper.DeleteSoft(object : SocketCallBack<Basic> {
                override fun next(body: Basic?) {
                    cont.safeResume(body)
                }

                override fun error(throwable: Throwable?) {
                    cont.safeResumeWithException(throwable)
                }
            }, appkey)
        }

    // -----------------------------------------------------------------
    //  User operations
    // -----------------------------------------------------------------

    /** Redeems a card / activation code. */
    suspend fun userPay(card: String): Basic =
        suspendCancellableCoroutine { cont ->
            SocketHelper.UserHelper.UserPay(object : SocketCallBack<Basic> {
                override fun next(body: Basic?) {
                    cont.safeResume(body)
                }

                override fun error(throwable: Throwable?) {
                    cont.safeResumeWithException(throwable)
                }
            }, card)
        }

    /** Changes the current user's password. */
    suspend fun changePass(oldPass: String, newPass: String): Basic =
        suspendCancellableCoroutine { cont ->
            SocketHelper.UserHelper.UserChangePass(object : SocketCallBack<Basic> {
                override fun next(body: Basic?) {
                    cont.safeResume(body)
                }

                override fun error(throwable: Throwable?) {
                    cont.safeResumeWithException(throwable)
                }
            }, oldPass, newPass)
        }

    /** Retrieves the current user's profile / dashboard info. */
    suspend fun getOther(): Other =
        suspendCancellableCoroutine { cont ->
            SocketHelper.UserHelper.GetOther(object : SocketCallBack<Other> {
                override fun next(body: Other?) {
                    cont.safeResume(body)
                }

                override fun error(throwable: Throwable?) {
                    cont.safeResumeWithException(throwable)
                }
            })
        }
}
