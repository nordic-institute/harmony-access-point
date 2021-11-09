import logging as log
import time

from zapv2 import ZAPv2
import requests

log.basicConfig(level=log.DEBUG)

WS_ENDPOINT = 'http://localhost:9088/domibus/services/wsplugin?wsdl'
MESS_ENDPOINT = 'http://localhost:9088/domibus/services/backend?wsdl'
SOAP_ENDPOINT_CONTEXT_REGEX = 'http://localhost:9088/domibus/services/.*'

apiKey = '8888'
zap_url = 'http://localhost:8281'
domibus_url = 'http://localhost:9088/domibus/'
plugin_auth_info = 'Basic c29hcFNjYW5TY3JpcHQ6UVchQHF3MTI='

localProxy = {"http": zap_url, "https": zap_url}

log.info("Starting ZAP configuration")
zap = ZAPv2(apikey=apiKey, proxies=localProxy)


def create_domibus_plugin_user():

	log.info("creating plugin user")

	headers = {"Accept": "application/json, text/plain, */*", "Content-Type": "application/json"}
	login_info = '{"username":"super","password":"123456"}'
	plugin_userName = 'soapScanScript'
	plugin_user_info = '[{"status":"NEW","userName":"soapScanScript","active":true,"suspended":false,"authenticationType":"BASIC","authRoles":"ROLE_ADMIN","password":"QW!@qw12"}]'
	plugin_auth_info = 'Basic c29hcFNjYW5TY3JpcHQ6UVchQHF3MTI='

	login_url = "http://localhost:9088/domibus/rest/security/authentication"
	plugin_user_url = "http://localhost:9088/domibus/rest/plugin/users"

	# login
	response = requests.post(url=login_url, headers=headers, data=str(login_info))

	cookies = response.cookies

	# get xsrf tocken
	xsrf_token = ""
	for cooki in cookies:
		if "XSRF-TOKEN" == cooki.name:
			xsrf_token = cooki.value

	headers['X-XSRF-TOKEN'] = xsrf_token

	# put the user
	response = requests.put(url=plugin_user_url, headers=headers, data=plugin_user_info, cookies=cookies)

	if response.status_code > 250 and response.status_code != 409:
		exit(1)


create_domibus_plugin_user()

log.info("Disabling pasive scanners")
zap.pscan.disable_all_scanners()
zap.pscan.disable_all_tags()
zap.pscan.set_enabled(enabled="false")

log.info("Creating new session")
zap.core.new_session(name="newSoapSeshPyScript", overwrite=True)


# getting context
log.info("Getting context")
context_id = zap.context.context("Default Context")['id']
log.debug("Context ID: " + context_id)


# load SOAP endpoint
log.info("Loading file with URLs to scan")
zap.soap.import_url(url=WS_ENDPOINT, apikey=apiKey)
zap.soap.import_url(url=MESS_ENDPOINT, apikey=apiKey)


zap.context.include_in_context(contextname="Default Context", regex=SOAP_ENDPOINT_CONTEXT_REGEX, apikey=apiKey)


zap.replacer.add_rule(description="authh", enabled=True, matchtype="REQ_HEADER", matchregex=False, matchstring="Authorization", replacement=plugin_auth_info, initiators=2, apikey=apiKey)

log.info("Enable all scan policies")
zap.ascan.enable_all_scanners()

# start and wait for active scan to finish
print("Active scan started for : " + WS_ENDPOINT)

# scanId = zap.ascan.scan(url=SOAP_ENDPOINT, recurse=False, inscopeonly=True, scanpolicyname=None, contextid=context_id, apikey=apiKey)
scanId = zap.ascan.scan(contextid=context_id, apikey=apiKey)


progress = 0
while (progress<100):
	time.sleep(30)
	progress = int(zap.ascan.status(scanId))
	log.info("Active Scan progress : " + str(progress) + "%")

log.info("Scan complete")


log.info("remove auth rule")
zap.replacer.remove_rule(description="authh", apikey=apiKey)



# print PDF report to file
log.info("Printing PDF report to file")
params = {"apikey": apiKey,
          "title": "SOAP Zap Scan Report",
          "template": "traditional-pdf",
          "description": "Scan of the SOAP endpoints for Web service plugins",
          "contexts": "Default Context",
          "reportFileName": "soap_zap_scan_report",
          "reportDir": os.getcwd()}

resp = requests.get(zap_url + "/JSON/reports/action/generate/", params=params)
if resp.status_code != 200:
	log.critical("CREATE PDF REPORT OPTERATION FAILED: ", resp)
	sys.exit(-1)
