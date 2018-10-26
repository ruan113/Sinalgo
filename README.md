# Sinalgo

Para utilizar o sinalgo recomenda-se a sua integração com uma IDE. A IDE recomendada pelo Sinalgo é o eclipse, sendo que esta foi utilizada para a realização deste trabalho na sua versão 2018-09 (4.9.0) e será abordada nesse tutorial.

Os requisitos para o Sinalgo funcionar corretamente são:

Java  JDK versão 5.0 ou superior;
Eclipse ou outra IDE de sua Preferência;
Apache ANT para tornar possível a construção do framework.

Para instalar o JDK  em sua versão mais recente utilize os seguintes comandos:

$ sudo apt-get update
$ sudo apt-get install default-jre
$ sudo apt-get install default-jdk

Para instalar o apache Ant

$ sudo apt-get install ant

Recomenda-se NÃO instalar o eclipse disponível no repositório padrão de sua distribuição portanto acesse o site do eclipse e baixe a versão mais recente e compatível com o seu sistema operacional https://www.eclipse.org/downloads/. Após realização do download do eclipse IDE descompacte o arquivo com o comando :

$ tar xzf nome_arquivo_eclipse
Ex:
$ tar xzf eclipse-inst-linux64.tar.gz

Acesse a pasta criada e execute o arquivo eclipse utilizando os comandos a seguir
$ ./eclipse-installer/eclipse-inst

Na janela de instalação selecione a opção para desenvolvimento java e siga as instruções na tela até o final. Ao término da instalação é necessário criar uma entrada de desktop para o eclipse, para isso utilize o comando a seguir: 

nano .local/share/applications/eclipse.desktop

Preencha o arquivo com as entradas a seguir:

[Desktop Entry]
Name=Eclipse JEE Oxygen
Type=Application
Exec=/home/#user/eclipse/jee-oxygen/eclipse/eclipse
Terminal=false
Icon=/home/#user/eclipse/jee-oxygen/eclipse/icon.xpm
Comment=Integrated Development Environment
NoDisplay=false
Categories=Development;IDE;
Name[en]=Eclipse

Modifique o nome de usuário (destacado acima) para o usuário do seu sistema operacional e outras linhas que não estejam de acordo com o seu ambiente de sistema operacional. Após este passo salve e feche o arquivo.

Para instalar o sinalgo baixe a última versão no site  https://sourceforge.net/projects/sinalgo/  e descompacte o arquivo baixado em um local que seu usuário do sistema operacional tenha permissão para realizar leitura e escrita dentro da pasta.

Após a etapa anterior baixe o código do projeto LEACH e LEACH-C, disponível no Github através do link: https://github.com/ruan113/Sinalgo.git 

Inicie o eclipse e escolha seu workspace. Após isto será apresentado a tela inicial da IDE. O próximo passo agora é importar o projeto sinalgo, para isto, no canto superior esquerdo do seu ambiente de desenvolvimento, clique em “File” e em seguida clique em “Open Projects from File System” e a tela “Import Projects from File System or Archive” será exibida. clique em “Diretory...”e localize o projeto sinalgo com o LEACH e LEACH-C que foi baixado anteriormente do Github. Após o diretório ser carregado, na tela “Import Projects from File System or Archive” clique no botão “Finish” e o projeto será carregado para o ambiente Eclipse.

Após importar o projeto, em “Package Explorer” do eclipse, dê um duplo clique na pasta do projeto (Sinalgo). Será listado todas as pastas pertencente ao projeto. “Navegue” agora por src->projects.leach->Config.xml e na linha 259 do arquivo Config.xml altere a variável Path para o local onde o seu projeto LEACH foi baixado

Código exemplo: <PathToLeach Path="/home/bruno/Documentos/redes/TRABALHO_FINAL/sinalgo/src/projects/leach"/>

Agora realize o mesmo passo para o LEACH-C. Em “Package Explorer” do eclipse, dê um duplo clique na pasta do projeto (Sinalgo). Será listado todas as pastas pertencente ao projeto. “Navegue” agora por src->projects.leach_c->Config.xml e na linha 259 do arquivo Config.xml altere a variável Path para o local onde o seu projeto LEACH-C foi baixado. Por exemplo:
<PathToLeach Path="/home/bruno/Documentos/redes/TRABALHO_FINAL/Sinalgo/src/projects"/>

  Caso o seu sistema operacional for o Linux, abra o arquivo CustomGlobal.java localizado em Sinalgo->src->projects.leach->CustomGlobal.java e também em Sinalgo->src->projects.leach_c->CustomGlobal.java e altera a constante SEPARETOR atribuindo o valor “/”, como no exemplo a seguir: 
Original:
public static String SEPARETOR = "\\";

Alterar para:
public static String SEPARETOR = "/";

Abra o terminal e navegue até a pasta leach do projeto que você importou, através do comando cd,  e execute o ant compile, para buildar o projeto. Faça o mesmo para a pasta leach_c

Para executar o projeto há duas maneiras: linha de comando ou pela eclipse IDE:
Se a sua variável de ambiente do sistema JAVA_HOME estiver setada então abra o terminal, acesse o diretório do projeto importado no eclipse e execute o comando “java -cp binaries/bin sinalgo.Run -project leach”

Caso a variável JAVA_HOME não esteja configurada ou se preferir executar o projeto pelo eclipse, em “Package Explorer”, clique com o botão direito do mouse sobre o projeto, depois clique em “Run AS” e em seguida clique em “1 Java Application”. Após esta etapa selecione “Run - sinalgo” e clique em ok

Abrirá a tela “Select a Project”

Para executar uma das simulações (LEACH ou LEACH-C) basta selecionar no canto esquerdo da tela ou o LEACH ou o LEACH-C. Após a seleção do algoritmo que deseja executar será exibido a tela inicial do projeto.

Para executar o projeto clique em Simulation->Generate Nodes (F3) abrirá a tela “Create new Nodes”

Como exemplo executaremos o LEACH com 100 nós, para isto altere o campo “Number of Node” para 100. Em “Distribution Model” selecione PositionFile

Clique em “ok” e em seguida selecione o arquivo “leachNodes100.pos”, localizado na pasta do leach (ou a do leach-c, caso esteja executando este algoritmo). O sinalgo gerará a rede que será testada com 100 nós, no qual os quadrados pretos serão os nós da rede e  os círculos coloridos serão os clusters head.

Próximo passo é configurar a estação base. Para isto, Clique em Simulation->Generate Nodes (F3). Altere o campo “Number of Nodes” para 1, em “Distribution Model” mantenha o valor PositionFile. Por último altere o campo “Node Implementation” para “leach:EstacaoBaseNode”. Clique em “ok”, sem seguida selecione o arquivo radioBase.pos localizado na pasta leach (ou leach_c caso esteja executando o leach-c) e clique em abrir.

A rede completa, com estação base representado por um losango preto no centro da tela é apresentado

Para executar a simulação do LEACH basta clicar no ícone "play"  localizado ao lado direito. O algoritmo em execução é exibido na figura 18, na qual cada região de coloração diferente representa um cluster e cada nó vermelho significa um “nó morto”, ou seja, esgotou sua energia residual.

Após a execução completa, o simulador exibirá todos os nós “mortos” (bateria descarregada) em vermelho
