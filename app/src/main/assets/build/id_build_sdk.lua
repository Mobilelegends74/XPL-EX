function after(hook, param)
    local original = param:getResult()
    local fake = param:getSetting("android.build.version.sdk")
    if fake == nil then
        return false
    end
    param:setResult(fake)
    return true, tostring(original), fake
end
