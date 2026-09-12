function after(hook, param)
    local uid = param:getArgument(0)
    local filtered = param:filterPackagesForUid(uid, param:getResult())
    param:setResult(filtered)
    return true
end
