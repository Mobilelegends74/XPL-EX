function after(hook, param)
    local original = param:getResult()
    local fake = param:getSetting("android.build.version.min.sdk")
    if fake == nil then
        return false
    end
    param:setResultToLongInt(fake)
    return true, tostring(original), fake
end
