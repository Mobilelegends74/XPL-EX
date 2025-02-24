function before(hook, param)
    local res = param:ensurePutIsSafe(false, "android_id", "unique.android.id")
    if res ~= nil and res == true then
        return true, param:getOldResult(), param:getNewResult()
    end
    if res == nil then
        param:isNullError(hook)
    end
    return false
end
