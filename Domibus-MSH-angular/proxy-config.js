const PROXY_CONFIG = {
  "/rest/**": {
    target: "http://localhost:8080",
    changeOrigin: true,
    pathRewrite: {"^/": "/domibus/"},
    secure: false,
    logLevel: "debug",
    // cookiePathRewrite: "/" // Doesn't work
    onProxyRes: function (proxyRes, req, res) {
      let cookies = proxyRes.headers["set-cookie"];
      if (cookies) {
        proxyRes.headers["set-cookie"] = cookies.map(cookie => cookie
          .replace("Path=/domibus/", "Path=/")
          .replace("Path=/domibus", "Path=/"))
          .replace("path=/domibus", "path=/");
        console.log('test');
      }
    },
  }
};

module.exports = PROXY_CONFIG;
