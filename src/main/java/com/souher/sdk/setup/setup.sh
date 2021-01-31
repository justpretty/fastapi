yum install -y python
mkdir -p /daemons
mkdir -p /downloads
cd /downloads
wget http://libslack.org/daemon/download/daemon-0.6.4.tar.gz
tar zxf daemon-0.6.4.tar.gz
cd daemon-0.6.4
make
make install-daemon
make install-daemon-conf

cat >/daemons/mem.sh <<EOF
while true ;do
python -c "import os ;os.system('''free |head -n 2 |tail -n 1|awk '{print (\\\$NF)*100/\\\$2}'>/tmp/mem ''')"
sleep 1
done
EOF

cat >/daemons/cpu.sh <<EOF
while true ;do
python -c "import os ;os.system('''cpu=\`top -bn 1 -i -c|head -n 3|tail -n 1|awk -F , '{print \\\$4}'|awk -F ' ' '{print \\\$1}'\`; [ -n "\\\$cpu" ] && echo \\\$cpu>/tmp/cpu ''')"
sleep 1
done
EOF

chmod 755 /daemons/mem.sh
chmod 755 /daemons/cpu.sh

daemon -n mem /daemons/mem.sh
daemon -n cpu /daemons/cpu.sh

