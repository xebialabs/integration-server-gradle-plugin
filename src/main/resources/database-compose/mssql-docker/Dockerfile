FROM microsoft/mssql-server-linux:2017-GA

ENV ACCEPT_EULA=Y
ENV SA_PASSWORD=Passw@rd!

RUN mkdir /dblibs
RUN mkdir /docker-entrypoint-initdb.d
COPY *.sql /docker-entrypoint-initdb.d

RUN ln -s /opt/mssql-tools/bin/sqlcmd /usr/local/bin/sqlcmd \
    && ln -s /opt/mssql-tools/bin/bcp /usr/local/bin/bcp
COPY docker-entrypoint.sh /usr/local/bin/
COPY docker-entrypoint-initdb.sh /usr/local/bin/

RUN chmod +x /usr/local/bin/docker-entrypoint.sh
RUN chmod +x /usr/local/bin/docker-entrypoint-initdb.sh

EXPOSE 1433

ENTRYPOINT ["docker-entrypoint.sh"]