/*
 Copyright 2009-2022 Igor Polevoy

 Licensed under the Apache License, Version 2.0 (the "License");
 you may not use this file except in compliance with the License.
 You may obtain a copy of the License at

 http://www.apache.org/licenses/LICENSE-2.0

 Unless required by applicable law or agreed to in writing, software
 distributed under the License is distributed on an "AS IS" BASIS,
 WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 See the License for the specific language governing permissions and
 limitations under the License.
 */

/*
 This file is a collection of unobtrusive JS that binds to link_to generated anchors typical for Ajax calls.

 author: Igor Polevoy
 */
const __aw_bind = (container) => {
    container.querySelectorAll('a[data-link=aw]')
        .forEach(a => a.addEventListener('click', __aw_support));
}
const __aw_support = (ev) => {
    ev.preventDefault();
    let anchor = ev.target;
    let destination = anchor.getAttribute("data-destination");
    let formId = anchor.getAttribute("data-form");
    let href = anchor.getAttribute("href");
    let method = anchor.getAttribute("data-method") || "get";
    let before = anchor.getAttribute("data-before");
    let after = anchor.getAttribute("data-after");
    let beforeArg = anchor.getAttribute("data-before-arg");
    let afterArg = anchor.getAttribute("data-after-arg");
    let error = anchor.getAttribute("data-error");
    let csrfToken = anchor.getAttribute("data-csrf-token");
    let csrfParam = anchor.getAttribute("data-csrf-param");
    let confirmMessage = anchor.getAttribute("data-confirm");

    if (confirmMessage != null ) {
        if(!confirm(confirmMessage)){
            return false;
        }
    }

    if (before != null) {
        eval(before)(beforeArg);
    }

    method = method.toLowerCase();

    //!ajax
    if (destination == null) {
        if (formId !== null) {
            let form = document.getElementById(formId);
            if (form !== null) {
                let originAction = form.getAttribute("action");
                let originMethod = form.getAttribute("method");
                if (originAction != null && href !== originAction) {
                    console.warn("The original form (id=" + formId + ") action is incorrect. " + href + " is used");
                }
                if (method !== form.getAttribute("method").toLowerCase()) {
                    console.warn("Required action '" + method + "' is incorrect. The original form (id=" + formId + ") method '" + originMethod + "' is used.");
                }
                form.setAttribute("action", href);
                form.submit();
            } else {
                console.error("Form not found with id=" + formId);
            }
            return false;
        }
        return true;
    }


    let contentType = "application/x-www-form-urlencoded";
    let encType;
    let data;
    if (formId !== null) {
        let form = document.getElementById(formId);
        encType = form.getAttribute("enctype");
        if (encType !== null) {
            contentType = encType.toLowerCase();
        }
        let formData = new FormData(form);
        if (method !== 'get' && csrfToken != null) {
            if (!formData.has(csrfToken)) {
                formData.set(csrfParam, csrfToken);
            }
        }
        if (contentType.startsWith("application/json")) {
            let object = {};
            formData.forEach((key,value) => {
                object[key] = value;
            })
            data = JSON.stringify(object);
        } else if (contentType.startsWith("application/x-www-form-urlencoded")) {
            let array = [];
            formData.forEach((key,value) => {
                array.push(key + "=" + encodeURIComponent(value));
            })
            data = array.join('&');
        }
    }

    const xmlHttpRequest = new XMLHttpRequest();

    let errorHandler = function (event) {
        if (error != null) {
            eval(error)(event.target.status, event.target.responseText );
        }
    }

    xmlHttpRequest.addEventListener("load", (event) => {
        event.preventDefault();
        if (event.target.status === 200) {
            if (destination != null) {
                let container = document.getElementById(destination);
                if (destination !== null) {
                    container.innerHTML = event.target.responseText;
                    __aw_bind(container);
                } else {
                    console.error("Container (id=" + destination + ") not found.");
                }
            }
            if (after != null) {
                eval(after)(afterArg, data);
            }
        } else {
            errorHandler(event);
        }
    });

    xmlHttpRequest.addEventListener("error", errorHandler);
    xmlHttpRequest.open(method, href, true);
    xmlHttpRequest.setRequestHeader("Content-Type", contentType);
    xmlHttpRequest.setRequestHeader("X-Requested-With", "XMLHttpRequest");
    xmlHttpRequest.send(data);

    return false;
}
window.addEventListener("load", function (e) {
    __aw_bind(document);
})