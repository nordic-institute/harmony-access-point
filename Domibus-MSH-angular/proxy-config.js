const PROXY_CONFIG = {
  "/rest/**": {
    target: "https://niis2-ap.lxd:8443",
    changeOrigin: true,
    secure: false,
    logLevel: "debug",
  }
};

module.exports = PROXY_CONFIG;
