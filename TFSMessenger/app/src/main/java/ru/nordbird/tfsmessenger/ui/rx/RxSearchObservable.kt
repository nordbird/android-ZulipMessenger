package ru.nordbird.tfsmessenger.ui.rx

import androidx.appcompat.widget.SearchView
import io.reactivex.Observable
import io.reactivex.subjects.BehaviorSubject
import io.reactivex.subjects.PublishSubject

object RxSearchObservable {

    fun fromView(searchView: SearchView): Observable<String> {
        val subject: BehaviorSubject<String> = BehaviorSubject.create()

        searchView.setOnQueryTextListener(object : SearchView.OnQueryTextListener {
            override fun onQueryTextSubmit(query: String?): Boolean {
                return onNext(subject, query)
            }

            override fun onQueryTextChange(newText: String?): Boolean {
                return onNext(subject, newText)
            }

        })
        return subject
    }

    private fun onNext(subject: BehaviorSubject<String>, query: String?): Boolean {
        if (query != null) {
            subject.onNext(query)
        }
        return true
    }

}