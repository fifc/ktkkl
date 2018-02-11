package hello

import com.google.protobuf.Message
import protofiles.protojava.CodeInProtos
import protofiles.protojava.CommonProtos

fun Robot.blockUser(blockUid: Long, period: Int = 15) {
    var req = CodeInProtos.BlockUserRequest.newBuilder()
    req.uid = blockUid
    req.period = period
    val c = req.clientInfoBuilder
    c.uid = uid
    c.version = 1000000300057L
    c.oem = "Kotlin"
    c.osVersion = "KT-${uid%1000}"
    c.deviceId = mqtt.clientId_
    c.gpsBuilder.latitude = lat
    c.gpsBuilder.longitude = lng
    val reply = CodeInProtos.BlockUserReply.newBuilder()
    val msg = req.build()
    println("request -> $msg")
    val start = System.currentTimeMillis()
    Http().gwCall("BlockUser", msg, reply, object: Http.GwCallback {
        override fun onReply(reply: Message) {
            if (reply is CodeInProtos.BlockUserReply) {
                val timeCost = System.currentTimeMillis() - start
                if (reply.hasErrInfo() && reply.errInfo.err != CommonProtos.ErrorCode.OK) {
                    println("error: ${reply.errInfo.msg}")
                    status = -1
                }
                println("BlockUser <- $reply")
            }
        }

        override fun onError() {
            val timeCost = System.currentTimeMillis() - start
            println("error block user! time ${timeCost}ms")
        }
    })
}

fun Robot.unblockUser(blockUid: Long) {
    var req = CodeInProtos.UnblockUserRequest.newBuilder()
    req.uid = blockUid
    val c = req.clientInfoBuilder
    c.uid = uid
    c.version = 1000000300057L
    c.oem = "Kotlin"
    c.osVersion = "KT-${uid%1000}"
    c.deviceId = mqtt.clientId_
    c.gpsBuilder.latitude = lat
    c.gpsBuilder.longitude = lng
    val reply = CodeInProtos.UnblockUserReply.newBuilder()
    val msg = req.build()
    println("request -> $msg")
    val start = System.currentTimeMillis()
    Http().gwCall("UnblockUser", msg, reply, object: Http.GwCallback {
        override fun onReply(reply: Message) {
            if (reply is CodeInProtos.UnblockUserReply) {
                val timeCost = System.currentTimeMillis() - start
                if (reply.hasErrInfo() && reply.errInfo.err != CommonProtos.ErrorCode.OK) {
                    println("error: ${reply.errInfo.msg}")
                    status = -1
                }
                println("UnblockUser <- $reply")
            }
        }

        override fun onError() {
            val timeCost = System.currentTimeMillis() - start
            println("error unblock user! time ${timeCost}ms")
        }
    })
}

fun Robot.getBlockedUserList() {
    var req = CodeInProtos.GetBlockedUserListRequest.newBuilder()
    val c = req.clientInfoBuilder
    c.uid = uid
    c.version = 1000000300057L
    c.oem = "Kotlin"
    c.osVersion = "KT-${uid%1000}"
    c.deviceId = mqtt.clientId_
    c.gpsBuilder.latitude = lat
    c.gpsBuilder.longitude = lng
    val reply = CodeInProtos.GetBlockedUserListReply.newBuilder()
    val msg = req.build()
    println("get blocked user list request -> $msg")
    val start = System.currentTimeMillis()
    Http().gwCall("GetBlockedUserList", msg, reply, object: Http.GwCallback {
        override fun onReply(reply: Message) {
            if (reply is CodeInProtos.GetBlockedUserListReply) {
                val timeCost = System.currentTimeMillis() - start
                if (reply.hasErrInfo() && reply.errInfo.err != CommonProtos.ErrorCode.OK) {
                    println("error: ${reply.errInfo.msg}")
                    status = -1
                }
                println("GetBlockedUserList <- $reply")
            }
        }

        override fun onError() {
            val timeCost = System.currentTimeMillis() - start
            println("error get blocked user list! time ${timeCost}ms")
        }
    })
}

fun Robot.testBlockUser() {
    blockUser(uid + 1)
    blockUser(uid + 2)
    blockUser(uid + 3)
    unblockUser(uid + 2)
    getBlockedUserList()
}
