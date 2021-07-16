xl.repository:
  artifacts:
    root: build/artifacts
  database:
    db-driver-classname: "com.ibm.db2.jcc.DB2Driver"
    db-url: "jdbc:db2://localhost:50000/xldrepo"
    db-username: "xldrepo"
    db-password: "xldrepo"
    leak-detection-threshold: 2 minutes
    max-pool-size: 10
