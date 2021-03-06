// Modifications copyright (C) 2017, Baidu.com, Inc.
// Copyright 2017 The Apache Software Foundation

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

import com.baidu.palo.analysis.Analyzer;
import com.baidu.palo.analysis.TupleDescriptor;
import com.baidu.palo.catalog.SchemaTable;
import com.baidu.palo.common.Config;
import com.baidu.palo.common.InternalException;
import com.baidu.palo.qe.ConnectContext;
import com.baidu.palo.thrift.TPlanNode;
import com.baidu.palo.thrift.TPlanNodeType;
import com.baidu.palo.thrift.TScanRangeLocations;
import com.baidu.palo.thrift.TSchemaScanNode;
import com.google.common.base.Objects;
import com.google.common.base.Objects.ToStringHelper;
import org.apache.logging.log4j.Logger;
import org.apache.logging.log4j.LogManager;

import java.net.InetAddress;
import java.net.UnknownHostException;
import java.util.List;

/**
 * Full scan of an SCHEMA table.
 */
public class SchemaScanNode extends ScanNode {
    private static final Logger LOG = LogManager.getLogger(SchemaTable.class);

    private final String tableName;
    private       String schemaDb;
    private       String schemaTable;
    private       String schemaWild;
    private       String user;
    private       String frontendIp;
    private       int    frontendPort;

    /**
     * Constructs node to scan given data files of table 'tbl'.
     */
    public SchemaScanNode(PlanNodeId id, TupleDescriptor desc) {
        super(id, desc, "SCAN SCHEMA");
        this.tableName = desc.getTable().getName();
    }

    @Override
    protected String debugString() {
        ToStringHelper helper = Objects.toStringHelper(this);
        return helper.addValue(super.debugString()).toString();
    }

    @Override
    public void finalize(Analyzer analyzer) throws InternalException {
        // Convert predicates to MySQL columns and filters.
        schemaDb = analyzer.getSchemaDb();
        schemaTable = analyzer.getSchemaTable();
        schemaWild = analyzer.getSchemaWild();
        user = analyzer.getUser();
        try {
            frontendIp = InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            throw new InternalException("get host failed.");
        }
        frontendPort = Config.rpc_port;
    }

    @Override
    protected void toThrift(TPlanNode msg) {
        msg.node_type = TPlanNodeType.SCHEMA_SCAN_NODE;
        msg.schema_scan_node = new TSchemaScanNode(desc.getId().asInt(), tableName);
        if (schemaDb != null) {
            msg.schema_scan_node.setDb(schemaDb);
        } else {
            if (tableName.equalsIgnoreCase("GLOBAL_VARIABLES")) {
                msg.schema_scan_node.setDb("GLOBAL");
            } else if (tableName.equalsIgnoreCase("SESSION_VARIABLES")) {
                msg.schema_scan_node.setDb("SESSION");
            }
        }

        if (schemaTable != null) {
            msg.schema_scan_node.setTable(schemaTable);
        }
        if (schemaWild != null) {
            msg.schema_scan_node.setWild(schemaWild);
        }
        if (user != null) {
            msg.schema_scan_node.setUser(user);
        }
        ConnectContext ctx = ConnectContext.get();
        if (ctx != null) {
            msg.schema_scan_node.setThread_id(ConnectContext.get().getConnectionId());
        }
        msg.schema_scan_node.setIp(frontendIp);
        msg.schema_scan_node.setPort(frontendPort);
    }

    /**
     * We query MySQL Meta to get request's data localtion
     * extra result info will pass to backend ScanNode
     */
    @Override
    public List<TScanRangeLocations> getScanRangeLocations(long maxScanRangeLength) {
        return null;
    }

    @Override
    public int getNumInstances() {
        return 1;
    }
}
