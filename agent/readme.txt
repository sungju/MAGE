Each plugin must be a format of jar(Java Archive).
Each jar file name must ends with .jar and must contains following items:

- Binary files with a format UbiMAGE can understand.
- descriptor.xml : describe information about jar file.
- additional files 


descriptor.xml example

<?xml version="1.0" ?>
<agent_info>
        <!-- $[] for System.getProperty() entry -->
        <!-- ${} for Internal envrionment variable -->
		<!-- Default value of mode is "call" -->
		<!-- Default value of version is "1.0" -->
		
		
		<agent name="MemInfo" class="mage.mon.MemInfo" version="1.0" mode="now" >
			<arg name="Test" value="Sample" />
			<arg name="Babo" value="Merong" />
		</agent>
</agent_info>