# to be executed with the command: wlstapi.cmd ../4.2-to-5.0-Weblogic-removeJDBCDatasources.py
connect('weblogic','weblogic1', 't3://localhost:7001')
print ''
print '======================================================'
print 'The script has been connected to the Admin Server'
print '======================================================'
print ''
 
edit()
startEdit()

cd('/')
cmo.destroyJDBCSystemResource(getMBean('/JDBCSystemResources/edeliveryNonXA'))
cmo.destroyJDBCSystemResource(getMBean('/JDBCSystemResources/eDeliveryDs'))

 
print ''
print '==========================================================='
print 'The script has deleted all JDBC datasources '
print '==========================================================='
print ''
 
activate()
 
# This is the end of the script