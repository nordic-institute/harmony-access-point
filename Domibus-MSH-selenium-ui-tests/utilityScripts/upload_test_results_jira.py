import json
import logging as log
import time
from datetime import datetime
import requests
from requests.auth import HTTPBasicAuth
import sys
import xml.etree.ElementTree as ET

log.basicConfig(level=log.INFO, format='%(asctime)s  %(levelname)-10s %(processName)s  %(name)s %(message)s',
                datefmt="%Y-%m-%d-%H-%M-%S")

STATUSES = {'PASS': 1, 'FAIL': 2, 'WIP': 3, 'BLOCKED': 4, 'UNEXECUTED': -1}
headers = {'Content-type': 'application/json', 'Accept': 'application/json'}

projectKey = 'EDELIVERY'
cycleNameStub = 'SEL_WC_MT_'
parentCycleId = 143
boardID = 15
buildNoSource = "http://localhost:9008/domibus/rest/application/info"
environment_config = "Docker_WeblogicCluster-Oracle-Multitenancy"
cycle_description = 'Conatins results of the REST test suite '

baseUrl = "https://ec.europa.eu/cefdigital/tracker"
projPath = "/rest/api/2/project/"
sprintPath = "/rest/agile/1.0/board/{}/sprint"
versionPath = "/rest/api/2/project/{}/version"
cyclePath = "/rest/zapi/latest/cycle/"
jobProgress = "/rest/zapi/latest/execution/jobProgress/"
executionPath = "/rest/zapi/latest/execution/"
searchPath = "/rest/api/2/search"

projID = -1
sprintID = -1
versionID = -1
versionName = ''
versionPerifx = 'Domibus '
buildNo = -1
versionNo = -1
executions = []


#  -------------- Info from Domibus
def get_build_no():
    log.info("Getting build and version no")
    global buildNo
    global versionNo
    if not buildNoSource:
        log.info("Source for build number and version number not found")
        exit(3)
    resp = requests.get(buildNoSource)
    jsonResp = json.loads(resp.text.replace(")]}',", ''))
    log.debug("response after sanitizing: " + str(jsonResp))
    versionNo = jsonResp['versionNumber']
    log.info("versionNo = " + versionNo)
    buildNo = jsonResp['version']
    log.info("buildNo = " + buildNo)


def get_version_name():
    log.info("Getting version name")
    global buildNo
    global versionNo
    global versionName
    global versionPerifx
    if not versionNo:
        if not buildNo:
            log.info("versionNo and buildNo are empty, cannot continue")
            exit(4)
        versionNo = buildNo[buildNo.index('[') + 1:buildNo.index('-SNAPSHOT')]
        log.info("Extracted versionNo from buildNo - " + versionNo)

    versionName = versionPerifx + versionNo
    log.info('versionName = ' + versionName)


#  -------------- Info from Jira
cookiesJar = ''


def login():
    log.info("login")
    global cookiesJar
    tmp_resp = requests.get(baseUrl, auth=HTTPBasicAuth(username, password))
    if tmp_resp.status_code != 200:
        log.info("Login responded with status code " + tmp_resp.status_code)
        exit(1)

    log.info("Login success")
    cookiesJar = tmp_resp.cookies


def get_proj_id():
    log.info("Getting project ID")
    global projID
    global projectKey
    if not cookiesJar:
        log.info("don't have login cookies, exiting")
        exit(2)
    tmp_resp = requests.get(baseUrl + projPath + projectKey, cookies=cookiesJar)
    projID = tmp_resp.json()['id']
    log.info('projID = ' + projID)


def get_version_id():
    log.info("Getting version id")
    global versionName
    global projectKey
    global versionID

    jira_versions = []

    last = False
    start = 0
    maxResults = 100
    params = {}
    params['query'] = versionName
    params['maxResults'] = maxResults
    params['status'] = 'unreleased'
    params['orderBy'] = 'releaseDate'

    while not last:
        log.info("Searching verisons...")

        params['startAt'] = start
        resp = requests.get(baseUrl + versionPath.format(projectKey), params=params, cookies=cookiesJar).json()
        last = resp['isLast']
        start += maxResults
        for value in resp['values']:
            if not value['released'] and versionName in value['name']:
                jira_versions.append(value)

    log.info('Jira version search produced {} results'.format(len(jira_versions)))
    log.debug(str(jira_versions))
    if len(jira_versions) == 0:
        log.info('Could not get version id from Jira')
        exit(5)
    versionID = jira_versions[0]['id']
    log.info('Identified Jira versionID ' + versionID)


def get_sprint_id():
    global sprintID
    log.info("Getting current sprint")
    params = {}
    params['state'] = 'active'
    resp = requests.get(baseUrl + sprintPath.format(boardID), params=params, cookies=cookiesJar)
    if resp.status_code != 200:
        log.info('Getting active sprint failed with status - ' + resp.status_code)
        exit(6)
    respJson = resp.json()
    log.debug(respJson)
    sprintID = respJson['values'][0]['id']
    log.info("Got sprint id: " + str(sprintID))


#  -------------- Info from Zephyr

def get_cycle_executions(cycle_id):
    log.info('Identifying cycle executions')
    global executions
    global executionPath
    params = {}
    params['action'] = 'expand'
    params['cycleId'] = cycle_id
    resp = requests.get(baseUrl + executionPath, params=params, cookies=cookiesJar)
    if resp.status_code != 200:
        log.info("Getting executions failed with status - " + resp.status_code)
        exit(7)
    log.debug(resp.json())
    executions.extend(resp.json()['executions'])
    log.info("found {} executions".format(str(len(executions))))


def find_execution_id(test_id):
    log.info("matching test id to execution id for " + test_id)
    global executions
    for execution in executions:
        txt = execution['summary']
        tmp_id = txt.split("-")[0].strip() + "-" + txt.split("-")[1].strip()
        if tmp_id == test_id:
            log.info('found matching execution with id ' + str(execution['id']))
            return execution['id']
    log.info("***** Could not match test id to an execution for test id " + str(test_id))


def execute_execution(test_result):
    global headers
    params = {'changeAssignee': False}
    if test_result['status'] not in STATUSES:
        log.info("Could not identify correct status id for " + test_result['status'])
        return

    params['status'] = STATUSES[test_result['status']]

    if not test_result['id']:
        log.info("Could not find test id in " + str(test_result))
        return

    if test_result['info']:
        params['comment'] = test_result['info'][0:600]

    execution_id = find_execution_id(test_result['id'])

    if not execution_id:
        log.info(" Test id not matched for " + test_result['id'])
        return

    resp = requests.put(baseUrl + executionPath + str(execution_id) + '/execute/', data=json.dumps(params),
                        cookies=cookiesJar, headers=headers)

    if resp.status_code != 200:
        log.debug(resp.text)
        log.info("Executiong execution failed with status - " + str(resp.status_code))


def wait_job_finish(token):
    log.info("checking progress of cycle clone job with id " + token)
    id = 0

    for tries in range(0, 60):
        log.info("waiting for job progress")
        time.sleep(5)
        resp = requests.get(baseUrl + jobProgress + token, cookies=cookiesJar, headers=headers)
        if resp.status_code != 200:
            log.info("Checking status failed with status " + resp.status_code)
            continue

        try:
            id = int((resp.json())['entityId'])
        except:
            log.debug('job progress response content - ' + resp.text)
            pass

        if int(id) > 0:
            log.info("id = " + str(id))
            break
    time.sleep(20)
    return id


def create_test_cycle():
    global parentCycleId
    global cycleNameStub
    cycleName = cycleNameStub + datetime.now().strftime("%d_%m_%Y-%H_%M")
    params = {'name': cycleName, 'clonedCycleId': parentCycleId, 'build': buildNo, 'environment': environment_config,
              'description': cycle_description, 'projectId': projID, 'versionId': versionID, 'sprintId': sprintID}
    log.info("Creating cycle with data - " + str(params))

    resp = requests.post(baseUrl + cyclePath, cookies=cookiesJar, headers=headers, data=json.dumps(params))

    if resp.status_code != 200:
        log.info('creating cycle failed with status ' + str(resp.status_code));
        exit(8)

    token = (resp.json())['jobProgressToken']
    log.info("job token " + token)
    return wait_job_finish(token)


#  -------------- Info from testng_results.xml file

def add_test_to_results(test):
    global results
    if test['id'] not in results:
        results[test['id']] = test
        return

    if test['status'] == 'PASS':
        return

    if test['status'] == 'SKIPPED':
        return

    if test['status'] == 'FAIL':
        results[test['id']]['info'] += test['info'] + "\n"
        results[test['id']]['status'] = "FAIL"


def parse_rest_testng_results():
    global results
    for tc in root.iter('test-method'):
        if 'description' not in tc.attrib:
            log.info("Could not find test id in description for - " + str(tc.attrib))
            continue
        test = {}
        test["id"] = tc.attrib['description']
        test["status"] = tc.attrib['status']
        test["info"] = ""
        if test["status"] == "PASS":
            add_test_to_results(test)
            continue

        for val in tc.iter('value'):
            test["info"] += val.text.strip()

        # for mess in tc.iter('message'):
        # 	splits = mess.text.strip().split("\n")
        # 	exception_mess = splits[0] #+ "\n" + splits[1] + "\n"
        # 	test["info"] += " - " + exception_mess

        add_test_to_results(test)


def parse_selenium_testng_results():
    global results
    for tc in root.iter('test-method'):
        if 'description' not in tc.attrib:
            log.info("Could not find test id in description for - " + str(tc.attrib))
            continue
        test = {}
        test["id"] = tc.attrib['description']
        test["status"] = tc.attrib['status']
        test["info"] = ""
        if test["status"] == "PASS":
            add_test_to_results(test)
            continue

        for val in tc.iter('full-stacktrace'):
            test["info"] += val.text.strip()
        add_test_to_results(test)


# ------------------- MAIN ---------------------
username = sys.argv[1]
password = sys.argv[2]
results_file = sys.argv[3]
profile = sys.argv[4]

log.info("Started")
log.info('Results file: ' + results_file)

root = ET.parse(results_file).getroot()
results = {}

if profile == "rest":
    parse_rest_testng_results()
else:
    parse_selenium_testng_results()

log.debug("Raw test results: " + str(results))

log.info("Login in Jira")
login()
get_proj_id()
log.info("Got project ID " + str(projID))

get_build_no()
log.info("Got build no: " + str(buildNo))

get_version_name()
log.info("Got expected version name: " + str(versionName))

get_version_id()
log.info("Got version ID: " + str(versionID))

get_sprint_id()
log.info("Got sprint ID: " + str(sprintID))

created_cycle_id = create_test_cycle()
log.info("Created test cycle with ID: " + str(created_cycle_id))
if created_cycle_id < parentCycleId:
    log.info("Most likely could not create cycle, will exit now!")
    exit(9)

get_cycle_executions(created_cycle_id)
log.info("Got executions: " + str(len(executions)))

for result in results:
    log.info("Uploading result: " + result)
    execute_execution(results[result])
