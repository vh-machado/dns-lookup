document.getElementById("dnsForm").addEventListener("submit", function (e) {
    e.preventDefault();

    const domain = document.getElementById("domain").value;

    fetch(`http://127.0.0.1:8080/dns?domain=${domain}`)
        .then((response) => {
            if (!response.ok) {
                throw new Error(`Erro na resposta do servidor DNS. Código de resposta: ${response.status}`);
            }
            return response.json();
        })
        .then((data) => {
            const resultElement = document.getElementById("result");
            resultElement.innerHTML = "";

            if (data.error) {
                resultElement.innerHTML = `<p>Erro na resposta do servidor DNS. Código de resposta: ${data.errorCode}</p>`;
            } else {
                const infoHTML = `<p><strong>Tipo de Registro:</strong> ${data.records[0].recordType}</p>
                                  <p><strong>Nome do Domínio:</strong> ${data.records[0].domainName}</p>
                                  <p><strong>Informações:</strong></p>
                                  <ul>`;

                data.records.forEach((record) => {
                    infoHTML += `<li><strong>Tipo:</strong> ${record.recordType}, <strong>Nome:</strong> ${record.domainName}, <strong>Dados:</strong> ${record.data}</li>`;
                });

                infoHTML += `</ul>`;
                resultElement.innerHTML = infoHTML;
            }
        })
        .catch((error) => {
            console.error("Erro ao fazer a solicitação:", error);
        });
});
