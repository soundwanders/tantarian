package com.soundwanders.tantarian

class ModelCategory {
    // matches the variables used in firebase store
    var id:String = ""
    var category:String = ""
    var timestamp:Long = 0
    var uid:String = ""

    // empty constructor called explicitly....
    // the fields of this class will be filled in using reflection
    // you cannot create an object with its fields pre-filled without a constructor
    // if you define your own constructor, default empty constructor no longer available to use
    constructor()

    constructor(id: String, category: String, timestamp: Long, uid: String) {
        this.id = id
        this.category = category
        this.timestamp = timestamp
        this.uid = uid
    }
}