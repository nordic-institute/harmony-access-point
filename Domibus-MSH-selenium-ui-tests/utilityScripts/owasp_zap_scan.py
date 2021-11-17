# java -jar .\zap-2.10.0.jar -daemon -port 8281 -config api.key=8888

from zapv2 import ZAPv2
import os
import logging as log
import time
import requests
import shutil


log.basicConfig(level=log.DEBUG)

apiKey = '8888'
zap_url = 'http://localhost:8281'
domibus_url = 'http://localhost:9088/domibus/'

contextFile = os.path.abspath("DOMIBUS_CONTEXT.context")
urlsFile = os.path.abspath("DOMIBUS_urls.txt")

localProxy = {"http": zap_url, "https": zap_url}

log.info("Starting ZAP configuration")
zap = ZAPv2(apikey=apiKey, proxies=localProxy)

log.info("Disabling pasive scanners")
zap.pscan.disable_all_scanners()
zap.pscan.disable_all_tags()
zap.pscan.set_enabled(enabled="false")

log.info("Creating new session")
zap.core.new_session(name="newSeshPyScript", overwrite=True)

# load context
log.info("Loading context file")
context_id = zap.context.import_context(contextfile=contextFile)
log.debug("Context ID: " + context_id)

# load URL's we want to scan
log.info("Loading file with URLs to scan")
zap.importurls.importurls(filepath=urlsFile)

# get user id (the only user associated with this context) for SUPER
log.info("Getting super user ID")
super_id = zap.users.users_list()[0]["id"]
log.debug("Super user ID is " + super_id)

# enable the user, after context import the user seems to be disabled
log.info("Enabling super user")
zap.users.set_user_enabled(contextid=context_id, userid=super_id, enabled="true")

log.info("Enable all scan policies")
zap.ascan.enable_all_scanners()

# start and wait for active scan to finish
print("Active scan started for : " + domibus_url)
scanId = zap.ascan.scan_as_user(contextid=context_id, userid=super_id, recurse=True, scanpolicyname=None, method=None, postdata=None)

progress = 0
while (progress<100):
	time.sleep(60)
	progress = int(zap.ascan.status(scanId))
	log.info("Active Scan progress : " + str(progress) + "%")

log.info("Scan complete")


# print PDF report to file
log.info("Printing PDF report to file")
params = {"apikey": apiKey,
          "title": "REST Zap Scan Report",
          "template": "traditional-pdf",
          "contexts": "DOMIBUS_CONTEXT",
          "reportFileName": "rest_zap_scan_report",
          "reportDir" : os.getcwd()}

resp = requests.get(zap_url + "/JSON/reports/action/generate/", params=params)
if resp.status_code != 200:
	log.critical("CREATE PDF REPORT OPTERATION FAILED: ", resp)
	exit(-1)
