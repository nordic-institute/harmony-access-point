import os
import shutil
import sys
import requests
import yaml
from requests.auth import HTTPBasicAuth
from yaml import dump

my_webserver_key = "wildfly"
my_url = "https://ec.europa.eu/cefdigital/code/projects/EDELIVERY/repos/docker/raw/development/corners/docker-compose-wf-mysql-mt.yml"
my_user = sys.argv[1]
my_pass = sys.argv[2]


# GET YML FILE CONTENT
params = {"at" : "refs/heads/development"}
tmp_resp = requests.get(my_url, params=params, auth=HTTPBasicAuth(my_user, my_pass))
content = tmp_resp.content

# Load in yaml obj and put correct ports for wildfly
data = yaml.unsafe_load(content)
obj = data['services'][my_webserver_key]
if "ports" in obj:
    del obj["ports"]

# set ports and wildfly container name (necessary for selfsending)
obj["ports"] = ["9088:8080"]
obj["container_name"] = "docker_local_wildfly"
output = dump(data)

# create docker folder and pour yaml content in it
folder_path = "../docker"
yml_path = folder_path + "/docker-compose.yml"

if os.path.exists(folder_path):
    shutil.rmtree(folder_path)

os.makedirs(folder_path)

with open(yml_path, 'w') as f:
    print(output, file=f)






