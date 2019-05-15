package com.zoiper.zdk.android.demokt.conference

import android.arch.lifecycle.MutableLiveData

/**
 *Extensions
 *
 *@since 3/04/2019
 */

operator fun <T> MutableLiveData<MutableList<T>>.plusAssign(values: List<T>) {
    val list = this.value ?: arrayListOf()
    list.addAll(values)
    this.value = list
}

operator fun <T> MutableLiveData<MutableList<T>>.plusAssign(value: T) {
    val list = this.value ?: arrayListOf()
    list.add(value)
    this.value = list
}
