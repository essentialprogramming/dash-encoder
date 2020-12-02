class Utils {

    constructor() {

    }

    static getRequestParameter(parameterName){
        if(parameterName = (new RegExp('[?&]' + encodeURIComponent(parameterName) + '=([^&]*)')).exec(location.search))
            return decodeURIComponent(parameterName[1]);
    }

    static docReady(fn) {
        // see if DOM is already available
        if (document.readyState === "complete" || document.readyState === "interactive") {
            // call on next available tick
            setTimeout(fn, 1);
        } else {
            document.addEventListener("DOMContentLoaded", fn);
        }
    }

    static loadComponent = (element, index) => {
        const url = element.getAttribute('data-include');
        const response = fetch(`${url}`)
        const html = response.text();
        element.innerHTML = html;


    }

}