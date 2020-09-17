import time
import requests
import sys

max_time = 3600
sleep_time = 30

status_code = 0
seconds_waited = 0
domibus_url = sys.argv[1]

start_waiting = time.time()

while status_code != 200 and seconds_waited < max_time:
	try:
		status_code = requests.get(domibus_url).status_code
	except:
		pass

	if status_code == 200:
		exit(0)

	print("Calling Domibus returned status code " + str(status_code))
	seconds_waited = int(time.time() - start_waiting)
	time.sleep(sleep_time)
	print("Been waiting for ", str(seconds_waited), " seconds")

exit(1)
