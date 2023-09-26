package com.example.udp;

import org.xbill.DNS.*;
import org.xbill.DNS.Record;

import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import java.io.IOException;
import java.io.PrintWriter;
import java.util.ArrayList;
import java.util.List;

public class DNSLookupServlet extends HttpServlet {
    @Override
    protected void doGet(HttpServletRequest request, HttpServletResponse response) throws ServletException, IOException {
        String domain = request.getParameter("domain");

        if (domain != null && !domain.isEmpty()) {
            try {
                List<DNSRecord> dnsRecords = new ArrayList<>();
                Lookup lookup = new Lookup(domain, Type.ANY);
                lookup.setResolver(new SimpleResolver("8.8.8.8")); // Use o servidor DNS do Google
                lookup.setCache(null); // Desativar o cache para setar como UDP

                Record[] records = lookup.run();

                if (lookup.getResult() == Lookup.SUCCESSFUL) {
                    for (Record record : records) {
                        DNSRecord dnsRecord = new DNSRecord();
                        dnsRecord.setRecordType(Type.string(record.getType()));
                        dnsRecord.setDomainName(record.getName().toString());
                        dnsRecord.setData(getRecordData(record));
                        dnsRecords.add(dnsRecord);
                    }

                    PrintWriter writer = response.getWriter();
                    response.setStatus(HttpServletResponse.SC_OK);
                    response.setContentType("application/json");

                    // Construir uma resposta JSON com base nas informações da consulta DNS
                    StringBuilder jsonResponse = new StringBuilder();
                    jsonResponse.append("{");
                    jsonResponse.append("\"records\": [");

                    for (int i = 0; i < dnsRecords.size(); i++) {
                        if (i > 0) {
                            jsonResponse.append(",");
                        }
                        jsonResponse.append("{");
                        jsonResponse.append("\"recordType\":\"").append(dnsRecords.get(i).getRecordType()).append("\",");
                        jsonResponse.append("\"domainName\":\"").append(dnsRecords.get(i).getDomainName()).append("\",");
                        jsonResponse.append("\"data\":\"").append(dnsRecords.get(i).getData()).append("\"");
                        jsonResponse.append("}");
                    }

                    jsonResponse.append("]");
                    jsonResponse.append("}");
                    writer.println(jsonResponse.toString());
                } else {
                    response.setStatus(HttpServletResponse.SC_NOT_FOUND);
                    response.getWriter().println("Nenhuma informação disponível para o domínio especificado.");
                }
            } catch (Exception e) {
                e.printStackTrace();
                response.setStatus(HttpServletResponse.SC_INTERNAL_SERVER_ERROR);
                response.getWriter().println("Erro no servidor DNS.");
            }
        } else {
            response.setStatus(HttpServletResponse.SC_BAD_REQUEST);
            response.getWriter().println("Parâmetro de domínio ausente.");
        }
    }

    // Método auxiliar para obter os dados do registro DNS
    private String getRecordData(Record record) {
        if (record instanceof ARecord) {
            return ((ARecord) record).getAddress().getHostAddress(); // IPv4
        } else if (record instanceof AAAARecord) {
            return ((AAAARecord) record).getAddress().getHostAddress(); // IPv6
        } else if (record instanceof MXRecord) {
            return ((MXRecord) record).getTarget().toString(); // MX
        } else if (record instanceof CNAMERecord) {
            return ((CNAMERecord) record).getName().toString(); // CNAME
        } else if (record instanceof TXTRecord) {
            return String.join(", ", ((TXTRecord) record).getStrings()); // TXT
        } else if (record instanceof NSRecord) {
            return ((NSRecord) record).getTarget().toString(); // NS
        }
        return "";
    }
}

class DNSRecord {
    private String recordType;
    private String domainName;
    private String data;

    // Getters and setters

    public String getRecordType() {
        return recordType;
    }

    public void setRecordType(String recordType) {
        this.recordType = recordType;
    }

    public String getDomainName() {
        return domainName;
    }

    public void setDomainName(String domainName) {
        this.domainName = domainName;
    }

    public String getData() {
        return data;
    }

    public void setData(String data) {
        this.data = data;
    }
}
