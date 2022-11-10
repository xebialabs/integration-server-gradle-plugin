FROM haproxy:latest
COPY dbhaproxy.cfg /usr/local/etc/haproxy/haproxy.cfg
USER root
RUN mkdir /run/haproxy &&\
    apt-get update -y &&\
    apt-get install -y hatop &&\
    apt-get install -y postgresql-client &&\
    apt-get clean
RUN chmod 777 /run/haproxy/
COPY entrypoint.sh /docker-entrypoint.sh
RUN chmod 755 /docker-entrypoint.sh
ENTRYPOINT ["/docker-entrypoint.sh"]
USER haproxy
CMD ["haproxy", "-f", "/usr/local/etc/haproxy/haproxy.cfg"]