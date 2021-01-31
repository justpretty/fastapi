i=8380
num=0
istart=8381

project=`pwd`
project=`basename $project`
date
cd /java/$project
>logrun.log
ii=$i
daemon --name=java_${project}_$ii --stop
for ((i=$istart; i<$[$num+$istart]; i++))
do
  daemon --name=java_${project}_$i --stop
done
sleep 1
cp -rf *0.9.9.jar java-$project.jar
daemon -r -n java_${project}_$ii -D /java/$project -o /java/$project/logrun.log -X "java -jar /java/$project/java-$project.jar $ii default"
for ((i=$istart; i<$[$num+$istart]; i++))
do
  daemon -r -n java_${project}_$i -D /java/$project -o /java/$project/logrun.log -X "java -jar /java/$project/java-$project.jar $i"
done
sleep 1
ps aux|grep java-$project|grep -v daemon|grep -v grep
#ps aux|grep java-$project|grep -v grep