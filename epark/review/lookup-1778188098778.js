(function(window, undefined) {
  var dictionary = {
    "fe1bafc1-757f-4310-8af0-52e50263fc48": "usuario.perfil",
    "d626423a-04cf-45bb-b4ae-07280ca290aa": "admin.zonas",
    "767e0479-a760-4ef1-bd16-4eb22e87d279": "admin.multas",
    "cac433cf-8040-4243-8a11-e1d4f6749193": "usuario.seleccionarVehiculo",
    "261e4db5-7111-410e-b5e1-1e17e321c96d": "admin.alertaExample",
    "3347ff7f-0fec-4de4-9719-5711f19aaecf": "usuario.notificacioens",
    "f410819e-4cc3-48ea-a8b2-5e7d1f828b28": "usuario.pagoSesion",
    "47a5d143-f765-4bf4-9ed0-d1608b3a033a": "usuario.sesionActivo",
    "a6ab8b36-ad82-4979-850f-cfafaed5231f": "usuaurio.extenderSesion",
    "26c999ff-09c7-4e11-ad96-893d4d68c98d": "usuario.editar",
    "7f421ace-e30f-464c-9615-c114d32005fb": "Admin.alerta",
    "2a7ccc36-630e-4717-b759-c18b70d4cb7b": "usuario.pagoExito",
    "073ae193-64be-4c77-8186-2e74d314f7bf": "usuario.pagarMulta",
    "17249bd7-015f-468d-8d2b-f218a0507cc0": "usuario.registroCarro2",
    "af1891dd-3e58-423d-8300-2a0bc5dab5f6": "usuario.agregarVehiculo",
    "cd24606e-37f2-4c10-a17b-00d78eb03e9b": "usuario.home",
    "4e52b4f9-39f4-4764-8449-31aee943f5bc": "admin.agregarZona",
    "92922524-9adf-413d-80b7-c44c3f7b8cd8": "registrar",
    "f17f71f6-5f62-40b4-b0a1-35f271024fbd": "usuario.registroCarro",
    "637002e2-5838-4aa0-af0a-8235d7ef3d07": "usuario.metodoDePago",
    "6d3568a6-761e-42c0-987b-33a5e7c38dca": "usuario.historial",
    "bf58a5f6-2528-4885-ac9d-248e7a281073": "Crear cuenta exito",
    "e093d0ef-53b2-48ab-a315-04f4b586dcbc": "salir",
    "bcc1c172-017c-4ed8-bf16-7e5b9371c2b0": "admin.gestionarZona",
    "38bd7dcf-0969-475f-870f-c1238e4912e8": "usuario.sesion",
    "66285efd-bbc1-4ffa-9104-16d14f523ca3": "usuario.agregarPago",
    "f9187d3e-8d35-41ba-b0ca-6072cd15ffc2": "usuario.multas",
    "d12245cc-1680-458d-89dd-4f0d7fb22724": "iniciar sesion",
    "42d18d8e-6f5d-41aa-bf3b-7f182907819f": "admin.reportes",
    "f39803f7-df02-4169-93eb-7547fb8c961a": "Template 1",
    "bb8abf58-f55e-472d-af05-a7d1bb0cc014": "Board 1"
  };

  var uriRE = /^(\/#)?(screens|templates|masters|scenarios)\/(.*)(\.html)?/;
  window.lookUpURL = function(fragment) {
    var matches = uriRE.exec(fragment || "") || [],
        folder = matches[2] || "",
        canvas = matches[3] || "",
        name, url;
    if(dictionary.hasOwnProperty(canvas)) { /* search by name */
      url = folder + "/" + canvas;
    }
    return url;
  };

  window.lookUpName = function(fragment) {
    var matches = uriRE.exec(fragment || "") || [],
        folder = matches[2] || "",
        canvas = matches[3] || "",
        name, canvasName;
    if(dictionary.hasOwnProperty(canvas)) { /* search by name */
      canvasName = dictionary[canvas];
    }
    return canvasName;
  };
})(window);