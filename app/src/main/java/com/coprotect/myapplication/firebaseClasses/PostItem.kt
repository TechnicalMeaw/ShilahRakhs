package com.coprotect.myapplication.firebaseClasses

import kotlin.properties.Delegates


class PostItem {
    var pTitle: String? = null
    var pId: String? = null
    var pDescription: String? = null
    var pLikes: String? = null
    var pImage: String? = null
    var pTime: String? = null
    var uid: String? = null
    var uDp: String? = null
    var uName: String? = null
    var pComments: String? = null

    constructor() {}
    constructor(
        pId: String?,
        pDescription: String?,
        pTitle : String?,
        pLikes: String?,
        pImage: String?,
        pTime: String?,
        uid: String?,
        uDp: String?,
        uName: String?,
        pComments: String?
    ) {
        this.pId = pId
        this.pDescription = pDescription
        this.pTitle = pTitle
        this.pLikes = pLikes
        this.pImage = pImage
        this.pTime = pTime
        this.uid = uid
        this.uDp = uDp
        this.uName = uName
        this.pComments = pComments
    }
}















/*package com.coprotect.myapplication.firebaseClasses

class PostItem(val userId: String,
               val postId: String,
               val postType: String,
               val postMediaUrl: String,
               val postCaption: String,
               val likeCount: Long,
               val commentCount: String,
               val postTimeInMillis: Long) {
    constructor(): this("", "", "", "", "", -1, "", -1)
}*/
