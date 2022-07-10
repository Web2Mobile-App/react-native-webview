async function handleRequest(details) {
  try {
    const cancel = await browser.runtime.sendNativeMessage("web2mobile_blocker", details);
    return {
      cancel,
    };
   } catch (error) {
    return {
      cancel: false
    };
   }
}

browser.webRequest.onBeforeRequest.addListener(
  handleRequest,
  {
    urls: [
      "<all_urls>"
    ],
    types: [
      "beacon",
      "csp_report",
      "font",
      "image",
      "imageset",
      "main_frame",
      "media",
      "object",
      "object_subrequest",
      "ping",
      "script",
      "speculative",
      "stylesheet",
      "sub_frame",
      "web_manifest",
      "websocket",
      "xbl",
      "xml_dtd",
      "xmlhttprequest",
      "xslt",
      "other"
    ]
  },
  [
    "blocking"
  ]
);