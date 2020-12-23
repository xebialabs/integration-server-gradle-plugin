#!/bin/bash

USERNAME="root"
PASSWORD="password"

ACCESS_TOKEN="access_token"

PROJECT_BASE_NAME=stitch

BASE_URL="http://localhost:11180"
BASE_URL_WITH_CREDENTIALS="http://$USERNAME:$PASSWORD@localhost:11180"
CURL=`which curl` 
GIT=`which git`
UNZIP=`which unzip`

BIGASS_REPO=stitch-huge-repo

GITLAB_TOKEN=`$CURL -s --data 'grant_type=password&username='$USERNAME'&password='$PASSWORD --request POST $BASE_URL/oauth/token | sed -e 's/[{}]/''/g' | awk 'BEGIN { FS="\""; RS="," }; { if ($2 == "'$ACCESS_TOKEN'") {print $4} }'`

function py_replace {
python << __END__
import glob
files = glob.glob('$PROJECT_BASE_NAME-$1/*.yaml')
files.extend(glob.glob('$PROJECT_BASE_NAME-$1/templates/*.ftl'))
for filename in files:
	with open(filename, 'r') as file :
	  filedata = file.read()
	filedata = filedata.replace('__seq_number__', '$1')
	with open(filename, 'w') as file:
	  file.write(filedata)
__END__
}

function py_scale_macro {
python << __END__
with open("$PROJECT_BASE_NAME-$1/macros/stitch-rules-marcro-template.yaml", 'r') as file :
  filedata = file.read()
filedata = filedata.replace('__seq_number__', '$1')
filedata = filedata.replace('__macro_seq_number__', '$2')
with open("$PROJECT_BASE_NAME-$1/macros/stitch-rules-marcro-$2.yaml", 'w') as file:
  file.write(filedata)
__END__
}

function py_simple_rule {
python << __END__
with open("template/simple-rule.yaml", 'r') as file :
  filedata = file.read()
filedata = filedata.replace('__seq_number__', '$1')
with open("$PROJECT_BASE_NAME-big-repo/stitch-rules-$1.yaml", 'w') as file:
  file.write(filedata)
__END__
}

function create_big_repo {
	$CURL -s -d '{"name":"'$PROJECT_BASE_NAME'-big-repo"}' -H "Content-Type: application/json" -X POST $BASE_URL/api/v4/projects?$ACCESS_TOKEN=$GITLAB_TOKEN >/dev/null 2>&1
	$GIT clone $BASE_URL_WITH_CREDENTIALS/root/$PROJECT_BASE_NAME-big-repo.git >/dev/null 2>&1
	for i in {1..100}; do
		py_simple_rule $i
	done
	$GIT -C $PROJECT_BASE_NAME-big-repo init . >/dev/null 2>&1
	$GIT -C $PROJECT_BASE_NAME-big-repo add . >/dev/null 2>&1
	$GIT -C $PROJECT_BASE_NAME-big-repo commit -m "Init commit" >/dev/null 2>&1
	$GIT -C $PROJECT_BASE_NAME-big-repo push >/dev/null 2>&1
	rm -rf $PROJECT_BASE_NAME-big-repo 
	echo "Created $PROJECT_BASE_NAME-big-repo"
}


function create_huge_repo {
  echo "Creating $BIGASS_REPO"
	$CURL -s -d '{"name":"'$PROJECT_BASE_NAME'-huge-repo"}' -H "Content-Type: application/json" -X POST $BASE_URL/api/v4/projects?$ACCESS_TOKEN=$GITLAB_TOKEN >/dev/null 2>&1
	echo "Fetching big-ass-repo from internet"
	$GIT clone https://github.com/spring-projects/spring-framework.git  >/dev/null 2>&1
	mv spring-framework $BIGASS_REPO
  echo "Creating repo for $BIGASS_REPO"
  cp -r stitch-for-huge/* $BIGASS_REPO/.
  echo "Adding stitch to $BIGASS_REPO"
  $GIT -C $BIGASS_REPO init >/dev/null 2>&1
  $GIT -C $BIGASS_REPO add . >/dev/null 2>&1
  $GIT -C $BIGASS_REPO commit -m "Adding the stitch to it" >/dev/null 2>&1
  echo "Changing the origin to $BIGASS_REPO"
  $GIT -C $BIGASS_REPO remote rename origin obsolete-origin
  $GIT -C $BIGASS_REPO remote add origin $BASE_URL_WITH_CREDENTIALS/root/stitch-huge-repo.git
  echo "Pushing to new origin for $BIGASS_REPO"
	$GIT -C $BIGASS_REPO push -u origin --all >/dev/null 2>&1
	$GIT -C $BIGASS_REPO push -u origin --tags >/dev/null 2>&1
	rm -rf $BIGASS_REPO
	echo "Created repo for $BIGASS_REPO"
}

create_huge_repo
create_big_repo




