FROM postgres:12.7

RUN apt-get update -y\
&& apt-get install python3 python3-pip -y\
&& apt-get install python3-dev libpq-dev -y\
&& pip3 install --upgrade pip\
&& pip3 install --upgrade setuptools\
&& pip3 install psycopg2-binary \
&& pip3 install patroni[zookeeper] \
&& mkdir /data/patroni -p \
&& chown postgres:postgres /data/patroni \
&& chmod 700 /data/patroni

#Copy patroni config files
COPY patroni.yaml /etc/patroni.yml

#Copy entrypoint1.sh file
COPY entrypoint.sh ./entrypoint.sh

USER postgres
ENTRYPOINT ["bin/sh", "/entrypoint.sh"]
