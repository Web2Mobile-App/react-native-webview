const port = browser.runtime.connectNative('web2mobile_document_start');
port.onMessage.addListener(function(message) {
    const javascript = message.javascript;
    if (!javascript || !javascript.length) {
        return;
    }
    Function('"use strict";return ' + javascript)();
});
window.ReactNativeWebView = port;