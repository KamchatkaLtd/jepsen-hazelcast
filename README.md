# jepsen.hazelcast

A Clojure library designed to ... well, that part is up to you.

## Usage

* Install Vagrant (and VirtualBox)
* Install Ansible (1.9+)
* `vagrant up`
* If vagrant hangs on "waiting for servers to shut down," stop it and 
** `vagrant provision`
* `vagrant ssh master`
* `cd /vagrant`
* `lein test`

When you change the test on the host machine, push the changes made to the master guest with `vagrant rsync`

Have fun!

## License

Copyright © 2016 Kamchatka Ltd.

Distributed under the Eclipse Public License either version 1.0 or (at
your option) any later version.
