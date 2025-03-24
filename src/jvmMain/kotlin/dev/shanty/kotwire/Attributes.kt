package dev.shanty.kotwire

import kotlinx.html.HTMLTag

var HTMLTag.dataController: String?
    get() = attributes["data-controller"]
    set(value) {
        if (value == null) {
            attributes.remove("data-controller")
        } else {
            attributes["data-controller"] = value
        }
    }
