function after(hook, param)
    local result = param:getResult()
    if result == nil then
        return false
    end

    local clsAm = luajava.bindClass('android.accounts.AccountManager')
    local am = clsAm:get(param:getApplicationContext())
    local auths = am:getAuthenticatorTypes()

    local restricted = true
    local packageName = param:getPackageName()
    for index = 1, auths['length'] do
        local auth = auths[index]
        if result.type == auth.type and auth.packageName == packageName then
            restricted = false
            break
        end
    end

    if restricted then
        local old = result.name
        local fake = param:getSetting('unique.account.email')
        if fake == nil then
            result.name = 'deadb33f@gmail.com'--private@lua.xprivacy.eu
        else
            result.name = fake
        end
        return true, old, fake
    else
        return false
    end
end
