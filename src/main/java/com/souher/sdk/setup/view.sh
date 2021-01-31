project=`pwd`
project=`basename $project`
date
cd /java/$project
ps aux|grep java-$project|grep -v grep