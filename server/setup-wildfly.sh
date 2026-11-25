#!/bin/sh
# Настраивает standalone.xml WildFly под выданный portbase (тот же скрипт, что в лабе 3).
# Запуск (сервер при этом должен быть ОСТАНОВЛЕН):
#   ./server/run.sh setup  (сам зальёт этот файл на Helios и запустит)
# или вручную на Helios: sh setup-wildfly.sh ~/wildfly-34.0.1.Final <portbase>
#
# Порты занимают диапазон portbase .. portbase+6, чтобы не пересечься с другими студентами:
#   http              = portbase      <- сюда ходит браузер, открыт для всех IP
#   https             = portbase + 1
#   management-http   = portbase + 2  <- админка, остаётся только на 127.0.0.1
#   management-https  = portbase + 3
#   ajp               = portbase + 4
#   txn-recovery-env  = portbase + 5
#   txn-status-manager= portbase + 6
set -e

WF="$1"
P="$2"
if [ -z "$WF" ] || [ -z "$P" ]; then
    echo "Использование: sh setup-wildfly.sh <папка WildFly> <portbase>"
    exit 1
fi

CLI="$WF/lab4-setup.cli"
cat > "$CLI" <<EOF
embed-server --server-config=standalone.xml --std-out=discard
/interface=public:write-attribute(name=inet-address, value=0.0.0.0)
/socket-binding-group=standard-sockets/socket-binding=http:write-attribute(name=port, value=$P)
/socket-binding-group=standard-sockets/socket-binding=https:write-attribute(name=port, value=$((P + 1)))
/socket-binding-group=standard-sockets/socket-binding=management-http:write-attribute(name=port, value=$((P + 2)))
/socket-binding-group=standard-sockets/socket-binding=management-https:write-attribute(name=port, value=$((P + 3)))
/socket-binding-group=standard-sockets/socket-binding=ajp:write-attribute(name=port, value=$((P + 4)))
/socket-binding-group=standard-sockets/socket-binding=txn-recovery-environment:write-attribute(name=port, value=$((P + 5)))
/socket-binding-group=standard-sockets/socket-binding=txn-status-manager:write-attribute(name=port, value=$((P + 6)))
stop-embedded-server
EOF

"$WF/bin/jboss-cli.sh" --file="$CLI"
rm -f "$CLI"
echo "Готово: http-порт $P открыт на 0.0.0.0, админка на 127.0.0.1:$((P + 2))"
