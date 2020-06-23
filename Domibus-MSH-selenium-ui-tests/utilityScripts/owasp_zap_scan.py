import json
import logging as log
import time
from datetime import datetime
import requests

# -------------------INIT & PROPERTIES-----------------------
log.basicConfig(level=log.INFO, format='%(asctime)s  %(levelname)s %(processName)s  %(name)s -- %(message)s',
                datefmt="%Y-%m-%d-%H-%M-%S")

false_alerts = []

domibus_url = 'http://ddomibus.com:9080/domibus/'
zapUrl = "http://localhost:8281"
day_of_week_to_run = 1

PASIVE_SCAN_STATUS_URL = '/JSON/pscan/view/recordsToScan/'
ACTIVE_SCAN_URL = '/JSON/ascan/action/scan'
ACTIVE_SCAN_STATUS_URL = '/JSON/ascan/view/status'
SCAN_ALERTS_URL = '/JSON/alert/view/alerts/'
DELETE_ALERT_URL = '/JSON/alert/action/deleteAlert/'
SCAN_REPORT_URL = '/OTHER/core/other/htmlreport'
SITES_URL = '/JSON/core/view/sites/'


# -------------------METHODS-----------------------
def get_pasive_scan_record_no():
    log.info('Getting number of records the passive scanner still has to scan')
    resp = requests.get(zapUrl + PASIVE_SCAN_STATUS_URL)
    log.debug(resp.text)
    return int(resp.json()['recordsToScan'])


def is_pasive_scan_done():
    status = get_pasive_scan_record_no() == 0
    log.info('Pasive scan done = ' + str(status))
    return status


def start_active_scan():
    log.info("Starting active scan for " + domibus_url);
    resp = requests.get(zapUrl + ACTIVE_SCAN_URL, params={'url': domibus_url}).json()
    log.debug(resp)
    wait_active_scan_end(resp['scan'])


def get_active_scan_procent(scan_id):
    log.info("Getting active scan progress for scan id " + str(scan_id));
    resp = requests.get(zapUrl + ACTIVE_SCAN_STATUS_URL, params={'scanID': scan_id}).json()
    log.debug(resp)
    return int(resp['status'])


def is_active_scan_done(scan_id):
    log.info("Checking if active scan is done ");
    return get_active_scan_procent(scan_id) == 100


def get_alerts(site):
    log.info("Getting all alerts for site " + site);
    resp = requests.get(zapUrl + SCAN_ALERTS_URL, params={'baseurl': site}).json()
    log.debug(resp)
    return resp['alerts']


def delete_alert(alert_id):
    resp = requests.get(zapUrl + DELETE_ALERT_URL, params={'id': alert_id})
    log.debug(resp.text)
    log.info("Delete alert with id " + str(alert_id) + " with status " + str(resp.status_code));
    return resp.status_code == 200


def wait_pasive_scan_end():
    log.info("Waiting for passive scan to finish")
    start_waiting = time.time()
    while not is_pasive_scan_done():
        time.sleep(60)
        wait_time = int(time.time() - start_waiting)
        log.info("Waiting for passive scan to finish for {} seconds".format(wait_time));


def wait_active_scan_end(scan_id):
    log.info("Waiting for active scan to finish")
    start_waiting = time.time()
    while not is_active_scan_done(scan_id):
        time.sleep(60)
        wait_time = int(time.time() - start_waiting)
        log.info("Waiting for active scan to finish for {} seconds".format(wait_time));


def get_sites():
    log.info("Getting all sites in session");
    resp = requests.get(zapUrl + SITES_URL).json()
    log.debug("got sites " + str(resp))
    return resp['sites']


def is_off_domain(site):
    log.info("Checking if site is to be cleansed " + site);
    return not (domibus_url in site or domibus_url == site or site in domibus_url)


def delete_off_domain_alerts():
    log.info("Deleting off domain alerts!!")
    sites = get_sites()
    for site in sites:
        if is_off_domain(site):
            log.info(site + "will have it's alerts deleted")
            alerts = get_alerts(site)
            log.info("got {} alerts".format(str(len(alerts))))
            for alert in alerts:
                delete_alert(alert['id'])


def is_false_alert(alert):
    log.info("checking if alert is listed in false alerts file")
    for false_alert in false_alerts:
        for key in false_alert:
            if key not in alert or false_alert[key] != alert[key]:
                log.info("not a flase alert" + alert)
                return False
    log.info("this false alert will be deleted from the session")
    return True


def delete_false_alerts():
    log.info('deleting false alerts')
    alerts = get_alerts(domibus_url)
    for alert in alerts:
        if is_false_alert(alert):
            delete_alert(alert['id'])


def get_HTML_report():
    log.info("Getting HTML report")
    delete_off_domain_alerts();
    delete_false_alerts();
    resp = requests.get(zapUrl + SCAN_REPORT_URL)
    with open('scan_report_{}.html'.format(str(datetime.now().strftime("%d_%m_%Y-%H_%M"))), 'w') as f:
        print(resp.text, file=f)


# -------------------MAIN-----------------------

log.info('Zap Client init ZAP_URL = {}, DOMIBUS_URL= {}'.format(zapUrl, domibus_url))
log.info('Configured day index to run is {}. Monday is index 0, Sunday is index 6'.format(str(day_of_week_to_run)))
log.info('Index of today is {}'.format(str(datetime.today().weekday())))

with open('false_alerts.json') as json_file:
    false_alerts = json.load(json_file)

log.info("false_alerts = " + str(false_alerts))

if datetime.today().weekday() != day_of_week_to_run:
    log.info("We are not running today!!!")
    exit(0)

log.info("TODAY IS THE DAY WE SCAN!!!")
wait_pasive_scan_end()
start_active_scan()
get_HTML_report()

