package com.example.fortune_whell_v3.api.services

import com.example.fortune_wheel_v3.api.RetrofitClient

object ApiServices {
    val adminService: AdminService by lazy {
        RetrofitClient.instance.create(AdminService::class.java)
    }
    val clienteService: ClienteService by lazy {
        RetrofitClient.instance.create(ClienteService::class.java)
    }
    val funcionarioService: FuncionarioService by lazy {
        RetrofitClient.instance.create(FuncionarioService::class.java)
    }
    val setupService: SetupService by lazy {
        RetrofitClient.instance.create(SetupService::class.java)
    }
        val loginService: LoginService by lazy {
        RetrofitClient.instance.create(LoginService::class.java)
    }
    val maquinaService: MaquinaService by lazy {
        RetrofitClient.instance.create(MaquinaService::class.java)
    }
    val premioService: PremioService by lazy {
        RetrofitClient.instance.create(PremioService::class.java)
    }
    val stockService: StockService by lazy {
        RetrofitClient.instance.create(StockService::class.java)
    }
    val talaoService: TalaoService by lazy {
        RetrofitClient.instance.create(TalaoService::class.java)
    }

    val levantamentoService: LevantamentoService by lazy {
        RetrofitClient.instance.create(LevantamentoService::class.java)
    }
}
