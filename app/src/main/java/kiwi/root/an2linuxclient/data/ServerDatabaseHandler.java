/*
 * Copyright 2017 rootkiwi
 *
 * AN2Linux-client is licensed under GNU General Public License 3.
 *
 * See LICENSE for more details.
 */

package kiwi.root.an2linuxclient.data;

/*
Current setup with database:
One database: servers.db
Five tables:
-----------------------
certificates
servers
wifi_servers
mobile_servers
bluetooth_servers
-----------------------

Table certificates is for certificates
Table servers is for server_id and common properties
and it's certificate_id is a foreign key referencing the id in certificates

Then the other tables include server type specific data
and their id is a foreign key referencing the id in table servers

Example:
sqlite> SELECT * FROM certificates;
_id         _certificate  _fingerprint
----------  ------------  ----------------------------------------
1           BLOB          68904029fac70b32fe8a9b963c23046d45ecc236
2           BLOB          b42532e754905f3378d90922b7561b9a32484ae6

sqlite> SELECT * FROM servers;
_id         _is_enabled  _certificate_id
----------  -----------  ---------------
1           1            1
2           0            2
3           1            2

sqlite> SELECT * FROM wifi_servers;
_id         _ip_or_hostname  _port       _ssid_whitelist
----------  ---------------  ----------  ---------------
1           192.168.15.40    31337       Wifi22

sqlite> SELECT * FROM mobile_servers;
_id         _ip_or_hostname  _port       _roaming_allowed
----------  ---------------  ----------  ----------------
2           mobile.com       44325       0

sqlite> SELECT * FROM bluetooth_servers;
_id         _bluetooth_mac_address  _bluetooth_name
----------  ----------------------  ----------------
3           28-DC-8F-B2-73-79       ChromeLinux_31AE
*/

import android.content.ContentValues;
import android.content.Context;
import android.database.Cursor;
import android.database.sqlite.SQLiteDatabase;
import android.database.sqlite.SQLiteOpenHelper;
import android.database.sqlite.SQLiteQueryBuilder;

import java.util.ArrayList;
import java.util.Collections;
import java.util.Formatter;
import java.util.List;

import kiwi.root.an2linuxclient.crypto.Sha1Helper;
import kiwi.root.an2linuxclient.interfaces.CertificateSpinnerItem;

public class ServerDatabaseHandler extends SQLiteOpenHelper {

    private static ServerDatabaseHandler sInstance;

    private static final int DATABASE_VERSION = 1;
    private static final String DATABASE_NAME = "servers.db";

    private final String TABLE_CERTIFICATES = "certificates";
    private final String TABLE_SERVERS = "servers";
    private final String TABLE_WIFI_SERVERS = "wifi_servers";
    private final String TABLE_MOBILE_SERVERS = "mobile_servers";
    private final String TABLE_BLUETOOTH_SERVERS = "bluetooth_servers";

    private final String COLUMN_ID = "_id";
    private final String COLUMN_CERTIFICATE = "_certificate";
    private final String COLUMN_FINGERPRINT = "_fingerprint";

    private final String COLUMN_IS_ENABLED = "_is_enabled";
    private final String COLUMN_CERTIFICATE_ID = "_certificate_id";

    private final String COLUMN_IP_OR_HOSTNAME = "_ip_or_hostname";
    private final String COLUMN_PORT_NUMBER = "_port";
    private final String COLUMN_SSID_WHITELIST = "_ssid_whitelist";

    private final String COLUMN_ROAMING_ALLOWED = "_roaming_allowed";

    private final String COLUMN_BLUETOOTH_MAC_ADDRESS = "_bluetooth_mac_address";
    private final String COLUMN_BLUETOOTH_NAME = "_bluetooth_name";

    private final String TRIGGER_TRIM_UNLINKED_CERTIFICATES = "trim_unlinked_certificates";


    private final String CREATE_TABLE_CERTIFICATES = "CREATE TABLE " + TABLE_CERTIFICATES + "(" +
            COLUMN_ID + " INTEGER PRIMARY KEY," +
            COLUMN_CERTIFICATE + " BLOB UNIQUE NOT NULL," +
            COLUMN_FINGERPRINT + " TEXT UNIQUE NOT NULL);";

    private final String CREATE_TABLE_SERVERS = "CREATE TABLE " + TABLE_SERVERS + "(" +
            COLUMN_ID + " INTEGER PRIMARY KEY," +
            COLUMN_IS_ENABLED + " INTEGER NOT NULL," +
            COLUMN_CERTIFICATE_ID + " INTEGER NOT NULL," +
            "FOREIGN KEY("+COLUMN_CERTIFICATE_ID+") REFERENCES "+TABLE_CERTIFICATES+"("+COLUMN_ID+"));";

    private final String CREATE_TABLE_WIFI_SERVERS = "CREATE TABLE " + TABLE_WIFI_SERVERS + "(" +
            COLUMN_ID + " INTEGER UNIQUE NOT NULL," +
            COLUMN_IP_OR_HOSTNAME + " TEXT NOT NULL," +
            COLUMN_PORT_NUMBER + " INTEGER NOT NULL," +
            COLUMN_SSID_WHITELIST + " TEXT," +
            "FOREIGN KEY("+COLUMN_ID+") REFERENCES "+TABLE_SERVERS+"("+COLUMN_ID+") ON DELETE CASCADE);";

    private final String CREATE_TABLE_MOBILE_SERVERS = "CREATE TABLE " + TABLE_MOBILE_SERVERS + "(" +
            COLUMN_ID + " INTEGER UNIQUE NOT NULL," +
            COLUMN_IP_OR_HOSTNAME + " TEXT NOT NULL," +
            COLUMN_PORT_NUMBER + " INTEGER NOT NULL," +
            COLUMN_ROAMING_ALLOWED + " INTEGER NOT NULL," +
            "FOREIGN KEY("+COLUMN_ID+") REFERENCES "+TABLE_SERVERS+"("+COLUMN_ID+") ON DELETE CASCADE);";

    private final String CREATE_TABLE_BLUETOOTH_SERVERS = "CREATE TABLE " + TABLE_BLUETOOTH_SERVERS + "(" +
            COLUMN_ID + " INTEGER UNIQUE NOT NULL," +
            COLUMN_BLUETOOTH_MAC_ADDRESS + " TEXT NOT NULL," +
            COLUMN_BLUETOOTH_NAME + " TEXT," +
            "FOREIGN KEY("+COLUMN_ID+") REFERENCES "+TABLE_SERVERS+"("+COLUMN_ID+") ON DELETE CASCADE);";

    private final String CREATE_TRIGGER_TRIM_UNLINKED_CERTIFICATES = "CREATE TRIGGER " + TRIGGER_TRIM_UNLINKED_CERTIFICATES +
            " AFTER DELETE ON " + TABLE_SERVERS +
            " BEGIN DELETE FROM " + TABLE_CERTIFICATES +
            " WHERE NOT EXISTS (SELECT " +
            COLUMN_CERTIFICATE_ID + " FROM " + TABLE_SERVERS +
            " WHERE " + TABLE_CERTIFICATES + "." + COLUMN_ID +
            "=" + TABLE_SERVERS + "." + COLUMN_CERTIFICATE_ID +
            "); END;";

    public static synchronized ServerDatabaseHandler getInstance(Context c) {
        if (sInstance == null) {
            sInstance = new ServerDatabaseHandler(c.getApplicationContext());
        }
        return sInstance;
    }

    /**
     * Constructor should be private to prevent direct instantiation.
     * make call to static method "getInstance()" instead.
     */
    private ServerDatabaseHandler(Context c) {
        super(c, DATABASE_NAME, null, DATABASE_VERSION);
    }

    @Override
    public void onCreate(SQLiteDatabase db) {
        db.execSQL(CREATE_TABLE_CERTIFICATES);
        db.execSQL(CREATE_TABLE_SERVERS);
        db.execSQL(CREATE_TABLE_WIFI_SERVERS);
        db.execSQL(CREATE_TABLE_MOBILE_SERVERS);
        db.execSQL(CREATE_TABLE_BLUETOOTH_SERVERS);
        db.execSQL(CREATE_TRIGGER_TRIM_UNLINKED_CERTIFICATES);
    }

    @Override
    public void onUpgrade(SQLiteDatabase db, int oldVersion, int newVersion) {
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_SERVERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_WIFI_SERVERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_MOBILE_SERVERS);
        db.execSQL("DROP TABLE IF EXISTS " + TABLE_BLUETOOTH_SERVERS);
        db.execSQL("DROP TRIGGER IF EXISTS " + TRIGGER_TRIM_UNLINKED_CERTIFICATES);
        onCreate(db);
    }

    @Override
    public void onConfigure(SQLiteDatabase db){
        db.setForeignKeyConstraintsEnabled(true);
    }

    private long addCertificate(byte[] certificateBytes){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_CERTIFICATE, certificateBytes);

        Formatter formatter = new Formatter();
        for (byte b : Sha1Helper.sha1(certificateBytes)){
            formatter.format("%02x", b);
        }
        values.put(COLUMN_FINGERPRINT, formatter.toString());

        long rowId = db.insert(TABLE_CERTIFICATES, null, values);

        db.close();
        return rowId;
    }

    public List<CertificateSpinnerItem> getSpinnerList(){
        List<CertificateSpinnerItem> spinnerList = new ArrayList<>();

        SQLiteDatabase db = this.getReadableDatabase();
        Cursor c = db.query(TABLE_CERTIFICATES,
                new String[]{COLUMN_ID, COLUMN_FINGERPRINT},
                null, null, null, null, null);

        if (c.moveToFirst()){
            do {
                spinnerList.add(new CertificateIdAndFingerprint(
                        c.getLong(0),
                        c.getString(1)));
            } while (c.moveToNext());
        }

        c.close();
        db.close();
        return spinnerList;
    }

    public boolean isThereAnyCertificatesInDatabase(){
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor c = db.query(TABLE_CERTIFICATES,
                new String[]{COLUMN_ID},
                null, null, null, null, null);

        boolean exists = c.getCount() > 0;
        c.close();
        db.close();
        return exists;
    }

    /**
     * @return certificate id or -1 if not found
     */
    public long getCertificateId(byte[] certificateBytes){
        Formatter formatter = new Formatter();
        for (byte b : Sha1Helper.sha1(certificateBytes)){
            formatter.format("%02x", b);
        }

        SQLiteDatabase db = this.getWritableDatabase();

        Cursor c = db.query(TABLE_CERTIFICATES,
                new String[]{COLUMN_ID},
                COLUMN_FINGERPRINT + "=?", new String[]{formatter.toString()},
                null, null, null);

        long returnValue;
        if (c.moveToFirst()){
            returnValue = c.getLong(0);
        } else {
            returnValue = -1;
        }
        c.close();
        db.close();
        return returnValue;
    }

    public String getCertificateFingerprint(long id){
        SQLiteDatabase db = this.getWritableDatabase();

        Cursor c = db.query(TABLE_CERTIFICATES,
                new String[]{COLUMN_FINGERPRINT},
                COLUMN_ID + "=?", new String[]{String.valueOf(id)},
                null, null, null);

        c.moveToFirst();
        String fingerprint = c.getString(0);

        c.close();
        db.close();

        return fingerprint;
    }

    // for TABLE_SERVERS
    private long addServer(long certificateId){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_ENABLED, boolToInt(true));
        values.put(COLUMN_CERTIFICATE_ID, certificateId);
        long rowId = db.insert(TABLE_SERVERS, null, values);

        db.close();
        return rowId;
    }

    public long addWifiServer(WifiServer wifiServer){
        return this.addWifiServer(wifiServer, this.addCertificate(wifiServer.getCertificateBytes()));
    }

    public long addWifiServer(WifiServer wifiServer, long certificateId){
        long serverId = this.addServer(certificateId);
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, serverId);
        values.put(COLUMN_IP_OR_HOSTNAME, wifiServer.getIpOrHostname());
        values.put(COLUMN_PORT_NUMBER, wifiServer.getPortNumber());
        values.put(COLUMN_SSID_WHITELIST, wifiServer.getSsidWhitelist());
        db.insert(TABLE_WIFI_SERVERS, null, values);

        db.close();
        return serverId;
    }

    public long addMobileServer(MobileServer mobileServer){
        return this.addMobileServer(mobileServer, this.addCertificate(mobileServer.getCertificateBytes()));
    }

    public long addMobileServer(MobileServer mobileServer, long certificateId){
        long serverId = this.addServer(certificateId);
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, serverId);
        values.put(COLUMN_IP_OR_HOSTNAME, mobileServer.getIpOrHostname());
        values.put(COLUMN_PORT_NUMBER, mobileServer.getPortNumber());
        values.put(COLUMN_ROAMING_ALLOWED, boolToInt(mobileServer.isRoamingAllowed()));
        db.insert(TABLE_MOBILE_SERVERS, null, values);

        db.close();
        return serverId;
    }

    public long addBluetoothServer(BluetoothServer bluetoothServer){
        return this.addBluetoothServer(bluetoothServer, this.addCertificate(bluetoothServer.getCertificateBytes()));
    }

    public long addBluetoothServer(BluetoothServer bluetoothServer, long certificateId){
        long serverId = this.addServer(certificateId);
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_ID, serverId);
        values.put(COLUMN_BLUETOOTH_MAC_ADDRESS, bluetoothServer.getBluetoothMacAddress());
        values.put(COLUMN_BLUETOOTH_NAME, bluetoothServer.getBluetoothName());
        db.insert(TABLE_BLUETOOTH_SERVERS, null, values);

        db.close();
        return serverId;
    }

    public void deleteServer(long id) {
        SQLiteDatabase db = this.getWritableDatabase();
        db.delete(TABLE_SERVERS,
                COLUMN_ID + "=?", new String[]{String.valueOf(id)});
        db.close();
    }

    public WifiServer getWifiServer(long id){
        SQLiteDatabase db = this.getReadableDatabase();

        WifiServer wifiServer = new WifiServer();
        wifiServer.setId(id);

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        qb.setTables(TABLE_SERVERS +
                " JOIN " + TABLE_WIFI_SERVERS +
                " USING(" + COLUMN_ID + ")");

        Cursor c = qb.query(db,
                new String[]{COLUMN_IS_ENABLED,
                        COLUMN_CERTIFICATE_ID,
                        COLUMN_IP_OR_HOSTNAME,
                        COLUMN_PORT_NUMBER,
                        COLUMN_SSID_WHITELIST},
                COLUMN_ID + "=?", new String[]{String.valueOf(id)},
                null, null, null);

        if (c.moveToFirst()){
            wifiServer.setIsEnabled(intToBool(c.getInt(0)));
            wifiServer.setCertificateId(c.getLong(1));
            wifiServer.setIpOrHostname(c.getString(2));
            wifiServer.setPortNumber(c.getInt(3));
            wifiServer.setSsidWhitelist(c.getString(4));
        }

        c.close();
        db.close();
        return wifiServer;
    }

    public MobileServer getMobileServer(long id){
        SQLiteDatabase db = this.getReadableDatabase();

        MobileServer mobileServer = new MobileServer();
        mobileServer.setId(id);

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        qb.setTables(TABLE_SERVERS +
                " JOIN " + TABLE_MOBILE_SERVERS +
                " USING(" + COLUMN_ID + ")");

        Cursor c = qb.query(db,
                new String[]{COLUMN_IS_ENABLED,
                        COLUMN_CERTIFICATE_ID,
                        COLUMN_IP_OR_HOSTNAME,
                        COLUMN_PORT_NUMBER,
                        COLUMN_ROAMING_ALLOWED},
                COLUMN_ID + "=?", new String[]{String.valueOf(id)},
                null, null, null);

        if (c.moveToFirst()){
            mobileServer.setIsEnabled(intToBool(c.getInt(0)));
            mobileServer.setCertificateId(c.getLong(1));
            mobileServer.setIpOrHostname(c.getString(2));
            mobileServer.setPortNumber(c.getInt(3));
            mobileServer.setRoamingAllowed(intToBool(c.getInt(4)));
        }

        c.close();
        db.close();
        return mobileServer;
    }

    public BluetoothServer getBluetoothServer(long id){
        SQLiteDatabase db = this.getReadableDatabase();

        BluetoothServer bluetoothServer = new BluetoothServer();
        bluetoothServer.setId(id);

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        qb.setTables(TABLE_SERVERS +
                " JOIN " + TABLE_BLUETOOTH_SERVERS +
                " USING(" + COLUMN_ID + ")");

        Cursor c = qb.query(db,
                new String[]{COLUMN_IS_ENABLED,
                        COLUMN_CERTIFICATE_ID,
                        COLUMN_BLUETOOTH_MAC_ADDRESS,
                        COLUMN_BLUETOOTH_NAME},
                COLUMN_ID + "=?", new String[]{String.valueOf(id)},
                null, null, null);

        if (c.moveToFirst()){
            bluetoothServer.setIsEnabled(intToBool(c.getInt(0)));
            bluetoothServer.setCertificateId(c.getLong(1));
            bluetoothServer.setBluetoothMacAddress(c.getString(2));
            bluetoothServer.setBluetoothName(c.getString(3));
        }

        c.close();
        db.close();
        return bluetoothServer;
    }

    public List<Server> getAllServers(){
        List<Server> allServers = new ArrayList<>();
        allServers.addAll(getAllMobileServers());
        allServers.addAll(getAllWifiServers());
        allServers.addAll(getAllBluetoothServers());

        Collections.sort(allServers);
        return allServers;
    }

    private List<WifiServer> getAllWifiServers(){
        List<WifiServer> allWifiServers = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        qb.setTables(TABLE_SERVERS +
                " JOIN " + TABLE_WIFI_SERVERS +
                " USING(" + COLUMN_ID + ")");

        Cursor c = qb.query(db,
                new String[]{COLUMN_ID,
                        COLUMN_IS_ENABLED,
                        COLUMN_CERTIFICATE_ID,
                        COLUMN_IP_OR_HOSTNAME,
                        COLUMN_PORT_NUMBER,
                        COLUMN_SSID_WHITELIST},
                null, null, null, null, null);

        if (c.moveToFirst()){
            do {
                WifiServer wifiServer = new WifiServer();
                wifiServer.setId(c.getLong(0));
                wifiServer.setIsEnabled(intToBool(c.getInt(1)));
                wifiServer.setCertificateId(c.getLong(2));
                wifiServer.setIpOrHostname(c.getString(3));
                wifiServer.setPortNumber(c.getInt(4));
                wifiServer.setSsidWhitelist(c.getString(5));
                allWifiServers.add(wifiServer);
            } while (c.moveToNext());
        }

        c.close();
        db.close();

        return allWifiServers;
    }

    private List<MobileServer> getAllMobileServers(){
        List<MobileServer> allWifiAndMobileServers = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        qb.setTables(TABLE_SERVERS +
                " JOIN " + TABLE_MOBILE_SERVERS +
                " USING(" + COLUMN_ID + ")");

        Cursor c = qb.query(db,
                new String[]{COLUMN_ID,
                        COLUMN_IS_ENABLED,
                        COLUMN_CERTIFICATE_ID,
                        COLUMN_IP_OR_HOSTNAME,
                        COLUMN_PORT_NUMBER,
                        COLUMN_ROAMING_ALLOWED},
                null, null, null, null, null);

        if (c.moveToFirst()){
            do {
                MobileServer wifiAndMobileServer = new MobileServer();
                wifiAndMobileServer.setId(c.getLong(0));
                wifiAndMobileServer.setIsEnabled(intToBool(c.getInt(1)));
                wifiAndMobileServer.setCertificateId(c.getLong(2));
                wifiAndMobileServer.setIpOrHostname(c.getString(3));
                wifiAndMobileServer.setPortNumber(c.getInt(4));
                wifiAndMobileServer.setRoamingAllowed(intToBool(c.getInt(5)));
                allWifiAndMobileServers.add(wifiAndMobileServer);
            } while (c.moveToNext());
        }

        c.close();
        db.close();

        return allWifiAndMobileServers;
    }

    private List<BluetoothServer> getAllBluetoothServers(){
        List<BluetoothServer> allBluetoothServers = new ArrayList<>();
        SQLiteDatabase db = this.getReadableDatabase();

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        qb.setTables(TABLE_SERVERS +
                " JOIN " + TABLE_BLUETOOTH_SERVERS +
                " USING(" + COLUMN_ID + ")");

        Cursor c = qb.query(db,
                new String[]{COLUMN_ID,
                        COLUMN_IS_ENABLED,
                        COLUMN_CERTIFICATE_ID,
                        COLUMN_BLUETOOTH_MAC_ADDRESS,
                        COLUMN_BLUETOOTH_NAME},
                null, null, null, null, null);

        if (c.moveToFirst()){
            do {
                BluetoothServer bluetoothServer = new BluetoothServer();
                bluetoothServer.setId(c.getLong(0));
                bluetoothServer.setIsEnabled(intToBool(c.getInt(1)));
                bluetoothServer.setCertificateId(c.getLong(2));
                bluetoothServer.setBluetoothMacAddress(c.getString(3));
                bluetoothServer.setBluetoothName(c.getString(4));
                allBluetoothServers.add(bluetoothServer);
            } while (c.moveToNext());
        }

        c.close();
        db.close();

        return allBluetoothServers;
    }

    public List<WifiServer> getAllEnabledWifiServers() {
        List<WifiServer> allEnabledWifiServers = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        qb.setTables(TABLE_SERVERS +
                " JOIN " + TABLE_WIFI_SERVERS +
                " USING(" + COLUMN_ID + ")" +
                " JOIN " + TABLE_CERTIFICATES + " ON " +
                TABLE_SERVERS + "." + COLUMN_CERTIFICATE_ID + "=" +
                TABLE_CERTIFICATES + "." + COLUMN_ID);

        Cursor c = qb.query(db,
                new String[]{COLUMN_CERTIFICATE,
                        COLUMN_IP_OR_HOSTNAME,
                        COLUMN_PORT_NUMBER,
                        COLUMN_SSID_WHITELIST},
                COLUMN_IS_ENABLED + "=?", new String[]{String.valueOf(boolToInt(true))},
                null, null, null);

        if (c.moveToFirst()) {
            do {
                WifiServer wifiServer = new WifiServer();
                wifiServer.setCertificate(c.getBlob(0));
                wifiServer.setIpOrHostname(c.getString(1));
                wifiServer.setPortNumber(c.getInt(2));
                wifiServer.setSsidWhitelist(c.getString(3));
                allEnabledWifiServers.add(wifiServer);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return allEnabledWifiServers;
    }

    public List<MobileServer> getAllEnabledMobileServers() {
        List<MobileServer> allEnabledWifiAndMobileServers = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        qb.setTables(TABLE_SERVERS +
                " JOIN " + TABLE_MOBILE_SERVERS +
                " USING(" + COLUMN_ID + ")" +
                " JOIN " + TABLE_CERTIFICATES + " ON " +
                TABLE_SERVERS + "." + COLUMN_CERTIFICATE_ID + "=" +
                TABLE_CERTIFICATES + "." + COLUMN_ID);

        Cursor c = qb.query(db,
                new String[]{COLUMN_CERTIFICATE,
                        COLUMN_IP_OR_HOSTNAME,
                        COLUMN_PORT_NUMBER,
                        COLUMN_ROAMING_ALLOWED},
                COLUMN_IS_ENABLED + "=?", new String[]{String.valueOf(boolToInt(true))},
                null, null, null);


        if (c.moveToFirst()){
            do {
                MobileServer mobileServer = new MobileServer();
                mobileServer.setCertificate(c.getBlob(0));
                mobileServer.setIpOrHostname(c.getString(1));
                mobileServer.setPortNumber(c.getInt(2));
                mobileServer.setRoamingAllowed(intToBool(c.getInt(3)));
                allEnabledWifiAndMobileServers.add(mobileServer);
            } while (c.moveToNext());
        }

        c.close();
        db.close();
        return allEnabledWifiAndMobileServers;
    }

    public List<BluetoothServer> getAllEnabledBluetoothServers() {
        List<BluetoothServer> allEnabledBluetoothServers = new ArrayList<>();
        SQLiteDatabase db = this.getWritableDatabase();

        SQLiteQueryBuilder qb = new SQLiteQueryBuilder();

        qb.setTables(TABLE_SERVERS +
                " JOIN " + TABLE_BLUETOOTH_SERVERS +
                " USING(" + COLUMN_ID + ")" +
                " JOIN " + TABLE_CERTIFICATES + " ON " +
                TABLE_SERVERS + "." + COLUMN_CERTIFICATE_ID + "=" +
                TABLE_CERTIFICATES + "." + COLUMN_ID);

        Cursor c = qb.query(db,
                new String[]{COLUMN_CERTIFICATE, COLUMN_BLUETOOTH_MAC_ADDRESS},
                COLUMN_IS_ENABLED + "=?", new String[]{String.valueOf(boolToInt(true))},
                null, null, null);

        if (c.moveToFirst()) {
            do {
                BluetoothServer bluetoothServer = new BluetoothServer();
                bluetoothServer.setCertificate(c.getBlob(0));
                bluetoothServer.setBluetoothMacAddress(c.getString(1));
                allEnabledBluetoothServers.add(bluetoothServer);
            } while (c.moveToNext());
        }
        c.close();
        db.close();
        return allEnabledBluetoothServers;
    }

    private void updateServerCertificateId(long serverId, long certificateId){
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_CERTIFICATE_ID, certificateId);
        db.update(TABLE_SERVERS, values,
                COLUMN_ID + "=?", new String[]{String.valueOf(serverId)});

        db.close();
    }

    public void updateWifiServer(WifiServer wifiServer){
        updateWifiServer(wifiServer, this.addCertificate(wifiServer.getCertificateBytes()));
    }

    public void updateWifiServer(WifiServer wifiServer, long certificateId) {
        updateServerCertificateId(wifiServer.getId(), certificateId);
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_IP_OR_HOSTNAME, wifiServer.getIpOrHostname());
        values.put(COLUMN_PORT_NUMBER, wifiServer.getPortNumber());
        values.put(COLUMN_SSID_WHITELIST, wifiServer.getSsidWhitelist());
        db.update(TABLE_WIFI_SERVERS, values,
                COLUMN_ID + "=?", new String[]{String.valueOf(wifiServer.getId())});

        db.close();
    }

    public void updateMobileServer(MobileServer mobileServer){
        updateMobileServer(mobileServer, this.addCertificate(mobileServer.getCertificateBytes()));
    }

    public void updateMobileServer(MobileServer mobileServer, long certificateId) {
        updateServerCertificateId(mobileServer.getId(), certificateId);
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_IP_OR_HOSTNAME, mobileServer.getIpOrHostname());
        values.put(COLUMN_PORT_NUMBER, mobileServer.getPortNumber());
        values.put(COLUMN_ROAMING_ALLOWED, boolToInt(mobileServer.isRoamingAllowed()));
        db.update(TABLE_MOBILE_SERVERS, values,
                COLUMN_ID + "=?", new String[]{String.valueOf(mobileServer.getId())});

        db.close();
    }

    public void updateBluetoothServer(BluetoothServer bluetoothServer){
        updateBluetoothServer(bluetoothServer, this.addCertificate(bluetoothServer.getCertificateBytes()));
    }

    public void updateBluetoothServer(BluetoothServer bluetoothServer, long certificateId) {
        updateServerCertificateId(bluetoothServer.getId(), certificateId);
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_BLUETOOTH_NAME, bluetoothServer.getBluetoothName());
        db.update(TABLE_BLUETOOTH_SERVERS, values,
                COLUMN_ID + "=?", new String[]{String.valueOf(bluetoothServer.getId())});

        db.close();
    }

    public void updateIsEnabled(long id, boolean isEnabled) {
        SQLiteDatabase db = this.getWritableDatabase();

        ContentValues values = new ContentValues();
        values.put(COLUMN_IS_ENABLED, boolToInt(isEnabled));
        db.update(TABLE_SERVERS, values,
                COLUMN_ID + "=?", new String[] {String.valueOf(id)});

        db.close();
    }

    private boolean intToBool(int num) {
        return num == 1;
    }

    private int boolToInt(boolean bool) {
        return bool ? 1 : 0;
    }

}
