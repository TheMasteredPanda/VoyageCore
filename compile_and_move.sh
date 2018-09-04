mvn clean package
rm /home/duke/Documents/Development/Minecraft/VoyagePvP/Server/plugins/VoyageCore-*.jar
mv /home/duke/Documents/Development/Java/VoyagePvP/VoyageCore/target/VoyageCore-*.jar /home/duke/Documents/Development/Minecraft/VoyagePvP/Server/plugins/
echo "Moved newly compliled jar to the testing server."
