const port = browser.runtime.connectNative('web2mobile_document_idle');
port.onMessage.addListener(function(message) {
    const javascript = message.javascript;
    if (!javascript) {
        return;
    }
    Function('"use strict";return ' + javascript)();
});
window.ReactNativeWebView = port;