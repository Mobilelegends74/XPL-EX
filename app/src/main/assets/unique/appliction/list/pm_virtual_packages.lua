function after(hook, param)
    local list = param:getResult()
    if list == nil then
        return false
    end
    param:setResult(param:mergeVirtualPackages(list))
    return true
end
