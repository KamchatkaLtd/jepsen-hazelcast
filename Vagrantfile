Vagrant.configure("2") do |config|

  N = 5

  config.vm.box = "debian/jessie64"

  config.vm.define "master" do |master|
    master.vm.network :private_network, :ip => "192.168.122.10", :mac => "5254008e29d2"
  end

  (1..N).each do |machine_id|
    config.vm.define "n#{machine_id}" do |db|
      db.vm.hostname = "n#{machine_id}"
      db.vm.network :private_network, :ip => "192.168.122.#{10 + machine_id}", :mac => "001E62AAAAA#{machine_id}"

      # Only execute once the Ansible provisioner,
      # when all the machines are up and ready.
      # see https://www.vagrantup.com/docs/provisioning/ansible.html
      if machine_id == N
      	db.vm.provision :ansible do |ansible|
          ansible.limit = "all"
          ansible.playbook = "playbook.yml"
          db_group_def = "n[1:#{N}]"
          ansible.groups = { "db" => [db_group_def] }
        end
      end
    end
  end
end
