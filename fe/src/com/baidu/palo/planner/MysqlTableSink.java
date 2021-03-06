// Copyright (c) 2017, Baidu.com, Inc. All Rights Reserved

// Licensed to the Apache Software Foundation (ASF) under one
// or more contributor license agreements.  See the NOTICE file
// distributed with this work for additional information
// regarding copyright ownership.  The ASF licenses this file
// to you under the Apache License, Version 2.0 (the
// "License"); you may not use this file except in compliance
// with the License.  You may obtain a copy of the License at
//
//   http://www.apache.org/licenses/LICENSE-2.0
//
// Unless required by applicable law or agreed to in writing,
// software distributed under the License is distributed on an
// "AS IS" BASIS, WITHOUT WARRANTIES OR CONDITIONS OF ANY
// KIND, either express or implied.  See the License for the
// specific language governing permissions and limitations
// under the License.

package com.baidu.palo.planner;

import com.baidu.palo.catalog.MysqlTable;
import com.baidu.palo.thrift.TDataSink;
import com.baidu.palo.thrift.TDataSinkType;
import com.baidu.palo.thrift.TExplainLevel;
import com.baidu.palo.thrift.TMysqlTableSink;

public class MysqlTableSink extends DataSink {
    private final String host;
    private final int port;
    private final String user;
    private final String passwd;
    private final String db;
    private final String tbl;

    public MysqlTableSink(MysqlTable mysqlTable) {
        host = mysqlTable.getHost();
        port = Integer.valueOf(mysqlTable.getPort());
        user = mysqlTable.getUserName();
        passwd = mysqlTable.getPasswd();
        db = mysqlTable.getMysqlDatabaseName();
        tbl = mysqlTable.getMysqlTableName();
    }

    @Override
    public String getExplainString(String prefix, TExplainLevel explainLevel) {
        return null;
    }

    @Override
    protected TDataSink toThrift() {
        TDataSink tDataSink = new TDataSink(TDataSinkType.MYSQL_TABLE_SINK);

        tDataSink.setMysql_table_sink(new TMysqlTableSink(host, port, user, passwd, db, tbl));
        return tDataSink;
    }

    @Override
    public PlanNodeId getExchNodeId() {
        return null;
    }

    @Override
    public DataPartition getOutputPartition() {
        return null;
    }
}
