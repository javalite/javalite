const __aw_bind = (container) => {
    container.querySelectorAll('a[data-link=aw]')
        .forEach(a => a.addEventListener('click', __aw_support));
}
const __aw_support = (ev) => {
    let anchor = ev.target;
    let destination = anchor.getAttribute("data-destination");
    let formId = anchor.getAttribute("data-form");
    let href = anchor.getAttribute("href");
    let method = (anchor.getAttribute("data-method") || "get").toLowerCase();
    let before = anchor.getAttribute("data-before");
    let after = anchor.getAttribute("data-after");
    let beforeArg = anchor.getAttribute("data-before-arg");
    let afterArg = anchor.getAttribute("data-after-arg");
    let error = anchor.getAttribute("data-error");
    let csrfToken = anchor.getAttribute("data-csrf-token");
    let csrfParam = anchor.getAttribute("data-csrf-param");
    let confirmMessage = anchor.getAttribute("data-confirm");
    let data = null;

    if (confirmMessage != null ) {
        if(!confirm(confirmMessage)){
            return false;
        }
    }

    if (before != null) {
        data = eval(before)(beforeArg);
    }

    ev.preventDefault();

    if (destination == null && after == null) {
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
        } else {
            let param = (form, key, value) => {
                let input = document.createElement("input");
                input.setAttribute("type", "hidden");
                input.setAttribute("name", key);
                input.setAttribute("value", value);
                form.append(input);
            }
            let form = document.createElement("form");
            form.setAttribute("method", method === "get" ? method : "post");
            form.setAttribute("action", href);
            if (["put", "delete"].indexOf(method) > -1) {
                param(form, "_method", method);
            }
            if (csrfToken) {
                param(form, csrfToken, csrfParam);
            }
            if (data !== null && data !== undefined && typeof data === "object") {
                for([key, val] of Object.entries(data)) {
                    param(form, key, val);
                }
            }
            document.body.append(form);
            form.submit();
        }
        return false;
    }

    let contentType = "application/x-www-form-urlencoded";
    let encType;
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
            for (let entry of formData.entries()) {
                array.push(encodeURIComponent(entry[0]) + '=' + encodeURIComponent(entry[1]));
            }
            if (array.length > 0) {
                if (href.indexOf('?') === -1) {
                    href += '?';
                } else {
                    href += '&'
                }
                href += new URLSearchParams(formData).toString();
            }
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
                eval(after)(afterArg, event.target.responseText);
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